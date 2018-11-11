package ru.cherryperry.instavideo.data.media.conversion

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
import android.media.MediaCodec
import android.media.MediaExtractor
import android.media.MediaFormat
import android.media.MediaMuxer
import android.media.ThumbnailUtils
import android.renderscript.Allocation
import android.renderscript.Element
import android.renderscript.RenderScript
import android.renderscript.ScriptIntrinsicYuvToRGB
import androidx.annotation.CheckResult
import com.google.android.exoplayer2.util.Util
import ru.cherryperry.instavideo.core.illegalArgument
import ru.cherryperry.instavideo.core.illegalState
import ru.cherryperry.instavideo.data.media.codec.MediaCodecFactory
import ru.cherryperry.instavideo.data.media.codec.dequeueInputBufferInfinite
import ru.cherryperry.instavideo.data.media.codec.dequeueOutputBufferInfinite
import ru.cherryperry.instavideo.data.media.extractor.MediaExtractorData
import ru.cherryperry.instavideo.data.media.extractor.getTrackData
import ru.cherryperry.instavideo.data.media.extractor.selectTrack
import ru.cherryperry.instavideo.data.media.extractor.unselectTrack
import ru.cherryperry.instavideo.renderscript.ScriptC_RgbToYuv
import java.nio.ByteBuffer
import javax.inject.Inject
import kotlin.math.min

class VideoConverterImpl @Inject constructor(
    private val context: Context
) : VideoConverter {

    companion object {

        /** 12 bits per pixel. **/
        fun yuvAllocationSize(width: Int, height: Int): Int {
            (width <= 0) illegalArgument "Width can't be negative or zero"
            (height <= 0) illegalArgument "Height can't be negative or zero"
            return width * height * 3 / 2
        }
    }

    private val mediaExtractor = MediaExtractor()

    override fun close() {
        mediaExtractor.release()
    }

    override fun process(
        source: MediaExtractorSource,
        startUs: Long,
        endUs: Long,
        sourceRect: RectF,
        outputFile: String,
        callback: VideoConverter.Callback
    ) {
        (startUs < 0) illegalArgument "Start can't be negative"
        (endUs < 0) illegalArgument "End can't be negative"
        (endUs < startUs) illegalArgument "End can't be before start"
        source.setDataSource(mediaExtractor)
        val tracks = selectTracks()
        // TODO Fix audio
        transcodeAndMux(tracks.first, null, startUs, endUs, sourceRect, outputFile, callback)
    }

    /** Read track's info from [MediaExtractor]. First one is video, second is audio. **/
    private fun selectTracks(): Pair<MediaExtractorData, MediaExtractorData?> {
        val trackCount = mediaExtractor.trackCount
        var videoData: MediaExtractorData? = null
        var audioData: MediaExtractorData? = null
        for (i in 0 until trackCount) {
            val data = mediaExtractor.getTrackData(i)
            if (videoData == null && data.mimeType.startsWith("video")) {
                videoData = data
            } else if (audioData == null && data.mimeType.startsWith("audio")) {
                audioData = data
            }
        }
        if (videoData == null) {
            throw IllegalArgumentException("No video input found!")
        }
        return Pair(videoData, audioData)
    }

    private fun transcodeAndMux(
        videoData: MediaExtractorData,
        audioData: MediaExtractorData?,
        startUs: Long,
        endUs: Long,
        sourceRect: RectF,
        outputFile: String,
        callback: VideoConverter.Callback
    ) {
        // peek video frame for additional data in output format
        val videoDecoder = MediaCodecFactory.createDecoderAndConfigure(videoData.mediaFormat).apply { start() }
        val videoFps = videoData.mediaFormat.getInteger(MediaFormat.KEY_FRAME_RATE)
        val videoEncoder = MediaCodecFactory.createVideoEncoder(videoFps).apply { start() }
        val rotation = if (videoData.mediaFormat.containsKey(MediaFormat.KEY_ROTATION)) {
            videoData.mediaFormat.getInteger(MediaFormat.KEY_ROTATION)
        } else {
            0
        }
        val videoFrameProcessor = ResizeRawFrameProcessor(context, videoEncoder, videoDecoder, sourceRect, rotation)
        val videoPeek = peekFirstFrame(videoData, videoDecoder, videoEncoder, startUs, videoFrameProcessor)
        // peek audio frame for additional data in output format
        val audioDecoder = if (audioData != null) {
            val decoder = MediaCodecFactory.createDecoderAndConfigure(audioData.mediaFormat).apply { start() }
            decoder
        } else {
            null
        }
        val audioEncoder = if (audioDecoder != null && audioData != null) {
            val sampleRate = audioData.mediaFormat.getInteger(MediaFormat.KEY_SAMPLE_RATE)
            val channelCount = audioData.mediaFormat.getInteger(MediaFormat.KEY_CHANNEL_COUNT)
            MediaCodecFactory.createAudioEncoder(sampleRate, channelCount).apply { start() }
        } else {
            null
        }
        val audioFrameProcessor = DefaultRawFrameProcessor()
        val audioPeek = if (audioEncoder != null && audioDecoder != null && audioData != null) {
            peekFirstFrame(audioData, audioDecoder, audioEncoder, startUs, audioFrameProcessor)
        } else {
            null
        }
        // start transcoding
        val mediaMuxer = MediaMuxer(outputFile, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4)
        val videoTrack = mediaMuxer.addTrack(videoEncoder.outputFormat)
        mediaMuxer.setOrientationHint(rotation)
        val audioTrack = if (audioData != null && audioEncoder != null) {
            mediaMuxer.addTrack(audioEncoder.outputFormat)
        } else {
            null
        }
        mediaMuxer.start()
        val frameCallback = ReportingFrameCallback(callback,
            min(endUs - startUs, videoData.mediaFormat.getLong(MediaFormat.KEY_DURATION)),
            if (audioTrack != null) 2 else 1)
        transcode(videoData, videoPeek, videoDecoder, videoEncoder, mediaMuxer, videoTrack, startUs, endUs,
            videoFrameProcessor, frameCallback)
        videoEncoder.stop()
        videoEncoder.release()
        videoDecoder.stop()
        videoDecoder.release()
        if (audioTrack != null && audioData != null && audioDecoder != null && audioEncoder != null && audioPeek != null) {
            frameCallback.switchTrack()
            transcode(audioData, audioPeek, audioDecoder, audioEncoder, mediaMuxer, audioTrack, startUs, endUs,
                audioFrameProcessor, frameCallback)
            audioEncoder.stop()
            audioEncoder.release()
            audioDecoder.stop()
            audioDecoder.release()
        }
        mediaMuxer.stop()
        mediaMuxer.release()
    }

    @CheckResult
    private fun peekFirstFrame(
        data: MediaExtractorData,
        decoder: MediaCodec,
        encoder: MediaCodec,
        startUs: Long,
        rawFrameProcessor: RawFrameProcessor
    ): PeekBufferIds {
        mediaExtractor.selectTrack(data)
        mediaExtractor.seekTo(startUs, MediaExtractor.SEEK_TO_PREVIOUS_SYNC)
        val bufferInfo = MediaCodec.BufferInfo()
        val byteBuffer = ByteBuffer.allocate(1024 * 1024)

        var encoderOutputBufferConfigurationIndex = -1
        val encoderOutputBufferConfigurationInfo = MediaCodec.BufferInfo()
        var encoderOutputBufferDataIndex = -1
        val encoderOutputBufferDataInfo = MediaCodec.BufferInfo()

        do {
            // read from source
            val read = mediaExtractor.readSampleData(byteBuffer, 0)
            if (read <= 0) {
                break
            }
            byteBuffer.limit(read)
            // decode
            val decoderInputBufferIndex = decoder.dequeueInputBufferInfinite()
            val decoderInputBuffer = decoder.getInputBuffer(decoderInputBufferIndex)!!
            decoderInputBuffer.position(0)
            decoderInputBuffer.put(byteBuffer)
            decoder.queueInputBuffer(decoderInputBufferIndex, 0, read, mediaExtractor.sampleTime, mediaExtractor.sampleFlags)
            var decoderOutputIndex = decoder.dequeueOutputBufferInfinite(bufferInfo)
            while (decoderOutputIndex == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED
                || decoderOutputIndex == MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED) {
                decoderOutputIndex = decoder.dequeueOutputBufferInfinite(bufferInfo)
            }
            if (mediaExtractor.sampleTime < startUs) {
                decoder.releaseOutputBuffer(decoderOutputIndex, false)
                continue
            }
            val decoderOutputBuffer = decoder.getOutputBuffer(decoderOutputIndex)!!
            // encode
            val encoderInputBufferIndex = encoder.dequeueInputBufferInfinite()
            val encoderInputBuffer = encoder.getInputBuffer(encoderInputBufferIndex)!!
            encoderInputBuffer.position(0)
            decoderOutputBuffer.position(bufferInfo.offset)
            decoderOutputBuffer.limit(bufferInfo.offset + bufferInfo.size)
            rawFrameProcessor.process(decoder, decoderOutputBuffer, bufferInfo, encoder, encoderInputBufferIndex, encoderInputBuffer)
            decoder.releaseOutputBuffer(decoderOutputIndex, false)
            // get output and save info about it
            var encoderOutputBufferIndex = encoder.dequeueOutputBufferInfinite(bufferInfo)
            while (encoderOutputBufferIndex == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED
                || encoderOutputBufferIndex == MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED) {
                encoderOutputBufferIndex = encoder.dequeueOutputBufferInfinite(bufferInfo)
            }
            if (bufferInfo.flags and MediaCodec.BUFFER_FLAG_CODEC_CONFIG != 0) {
                encoderOutputBufferConfigurationIndex = encoderOutputBufferIndex
                encoderOutputBufferConfigurationInfo.set(bufferInfo.offset, bufferInfo.size,
                    bufferInfo.presentationTimeUs, bufferInfo.flags)
                encoderOutputBufferDataIndex = encoder.dequeueOutputBufferInfinite(bufferInfo)
                encoderOutputBufferDataInfo.set(bufferInfo.offset, bufferInfo.size,
                    bufferInfo.presentationTimeUs, bufferInfo.flags)
            } else {
                encoderOutputBufferDataIndex = encoderOutputBufferIndex
                encoderOutputBufferDataInfo.set(bufferInfo.offset, bufferInfo.size,
                    bufferInfo.presentationTimeUs, bufferInfo.flags)
            }
            break
        } while (mediaExtractor.advance())
        mediaExtractor.unselectTrack(data)
        return PeekBufferIds(
            encoderOutputBufferConfigurationIndex,
            if (encoderOutputBufferConfigurationIndex == -1) null else encoderOutputBufferConfigurationInfo,
            encoderOutputBufferDataIndex,
            encoderOutputBufferDataInfo)
    }

    private fun transcode(
        data: MediaExtractorData,
        peekBufferIds: PeekBufferIds,
        decoder: MediaCodec,
        encoder: MediaCodec,
        mediaMuxer: MediaMuxer,
        mediaMuxerTrack: Int,
        startUs: Long,
        endUs: Long,
        rawFrameProcessor: RawFrameProcessor,
        frameCallback: FrameCallback?
    ) {
        mediaExtractor.selectTrack(data)
        mediaExtractor.seekTo(startUs, MediaExtractor.SEEK_TO_PREVIOUS_SYNC)

        // write peeked data
        if (peekBufferIds.configurationInfo != null) {
            mediaMuxer.writeSampleData(
                mediaMuxerTrack,
                encoder.getOutputBuffer(peekBufferIds.configurationId)!!,
                peekBufferIds.configurationInfo
            )
            encoder.releaseOutputBuffer(peekBufferIds.configurationId, false)
        }
        mediaMuxer.writeSampleData(
            mediaMuxerTrack,
            encoder.getOutputBuffer(peekBufferIds.dataId)!!,
            peekBufferIds.dataInfo
        )
        encoder.releaseOutputBuffer(peekBufferIds.dataId, false)

        val bufferInfo = MediaCodec.BufferInfo()
        val byteBuffer = ByteBuffer.allocate(1024 * 1024)
        do {
            // read from source
            byteBuffer.position(0)
            val read = mediaExtractor.readSampleData(byteBuffer, 0)
            if (read <= 0) {
                break
            }
            byteBuffer.limit(read)
            if (mediaExtractor.sampleTime == peekBufferIds.dataInfo.presentationTimeUs) {
                // this frame was processed in peek
                continue
            }
            if (mediaExtractor.sampleTime > endUs) {
                break
            }
            // decode
            val decoderInputBufferIndex = decoder.dequeueInputBufferInfinite()
            val decoderInputBuffer = decoder.getInputBuffer(decoderInputBufferIndex)!!
            decoderInputBuffer.position(0)
            decoderInputBuffer.put(byteBuffer)
            decoder.queueInputBuffer(decoderInputBufferIndex, 0, read, mediaExtractor.sampleTime, mediaExtractor.sampleFlags)
            var decoderOutputIndex = decoder.dequeueOutputBufferInfinite(bufferInfo)
            if (decoderOutputIndex == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED
                || decoderOutputIndex == MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED) {
                decoderOutputIndex = decoder.dequeueOutputBufferInfinite(bufferInfo)
            }
            if (mediaExtractor.sampleTime < startUs) {
                decoder.releaseOutputBuffer(decoderOutputIndex, false)
                continue
            }
            val decoderOutputBuffer = decoder.getOutputBuffer(decoderOutputIndex)!!
            // encode
            val encoderInputBufferIndex = encoder.dequeueInputBufferInfinite()
            val encoderInputBuffer = encoder.getInputBuffer(encoderInputBufferIndex)!!
            encoderInputBuffer.position(0)
            decoderOutputBuffer.position(bufferInfo.offset)
            decoderOutputBuffer.limit(bufferInfo.offset + bufferInfo.size)
            rawFrameProcessor.process(decoder, decoderOutputBuffer, bufferInfo, encoder, encoderInputBufferIndex, encoderInputBuffer)
            decoder.releaseOutputBuffer(decoderOutputIndex, false)
            // get output and write to file
            var encoderOutputBufferIndex = encoder.dequeueOutputBufferInfinite(bufferInfo)
            if (encoderOutputBufferIndex == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED
                || encoderOutputBufferIndex == MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED) {
                encoderOutputBufferIndex = encoder.dequeueOutputBufferInfinite(bufferInfo)
            }
            var encoderOutputBuffer = encoder.getOutputBuffer(encoderOutputBufferIndex)!!
            if (bufferInfo.flags and MediaCodec.BUFFER_FLAG_CODEC_CONFIG != 0) {
                mediaMuxer.writeSampleData(mediaMuxerTrack, encoderOutputBuffer, bufferInfo)
                encoder.releaseOutputBuffer(encoderOutputBufferIndex, false)
                encoderOutputBufferIndex = encoder.dequeueOutputBufferInfinite(bufferInfo)
                encoderOutputBuffer = encoder.getOutputBuffer(encoderOutputBufferIndex)!!
            }
            mediaMuxer.writeSampleData(mediaMuxerTrack, encoderOutputBuffer, bufferInfo)
            encoder.releaseOutputBuffer(encoderOutputBufferIndex, false)
            frameCallback?.onFrameCompleted(mediaExtractor.sampleTime - startUs)
        } while (mediaExtractor.advance())
        mediaExtractor.unselectTrack(data)
    }

    /** Video frames must be resized, audio - not. **/
    private interface RawFrameProcessor {

        /** Implementation should call [MediaCodec.queueInputBuffer] on [encoder] by itself. **/
        fun process(
            decoder: MediaCodec,
            decoderOutputBuffer: ByteBuffer,
            decoderOutputBufferInfo: MediaCodec.BufferInfo,
            encoder: MediaCodec,
            encoderInputBufferId: Int,
            encoderInputBuffer: ByteBuffer
        )
    }

    private class DefaultRawFrameProcessor : RawFrameProcessor {

        override fun process(
            decoder: MediaCodec,
            decoderOutputBuffer: ByteBuffer,
            decoderOutputBufferInfo: MediaCodec.BufferInfo,
            encoder: MediaCodec,
            encoderInputBufferId: Int,
            encoderInputBuffer: ByteBuffer
        ) {
            for (i in 0..decoderOutputBufferInfo.size) {
                val byte = decoderOutputBuffer.get(decoderOutputBufferInfo.offset + i)
                encoderInputBuffer.put(byte)
            }
            encoder.queueInputBuffer(encoderInputBufferId, 0, encoderInputBuffer.limit(),
                decoderOutputBufferInfo.presentationTimeUs, 0)
        }
    }

    /**
     * Resize and crop frame from decoder's size to encoder's.
     * YUV operations are hard, so convert it to RGB first, and make it easy with [Bitmap].
     */
    private class ResizeRawFrameProcessor(
        context: Context,
        encoder: MediaCodec,
        decoder: MediaCodec,
        sourceRect: RectF,
        rotation: Int
    ) : RawFrameProcessor {

        // input info, size won't change
        private val inputWidth = decoder.outputFormat.getInteger(MediaFormat.KEY_WIDTH)
        private val inputHeight = decoder.outputFormat.getInteger(MediaFormat.KEY_HEIGHT)

        private val outputWidth = encoder.outputFormat.getInteger(MediaFormat.KEY_WIDTH)
        private val outputHeight = encoder.outputFormat.getInteger(MediaFormat.KEY_HEIGHT)

        // render script
        private val renderScript = RenderScript.create(context)

        // resuable buffers
        private val inputYuvBuffer = ByteArray(yuvAllocationSize(inputWidth, inputHeight))
        private val inputBitmap = Bitmap.createBitmap(inputWidth, inputHeight, Bitmap.Config.ARGB_8888)
        private val outputBitmap = Bitmap.createBitmap(outputWidth, outputHeight, Bitmap.Config.ARGB_8888)
        private val outputBitmapCanvas = Canvas(outputBitmap)
        private val outputBitmapPaint = Paint()
        private val outputBitmapDst = RectF(sourceRect)
        private val outputBitmapSrc = Rect(0, 0, inputBitmap.width, inputBitmap.height)
        private val outputYuvBuffer = ByteArray(yuvAllocationSize(outputWidth, outputHeight))
        private val outputYuvAllocation = Allocation.createSized(renderScript, Element.U8(renderScript), outputYuvBuffer.size)

        init {
            if (outputBitmapDst.width() != 0f && outputBitmapDst.height() != 0f) {
                val matrix = Matrix()
                matrix.setRotate(-rotation.toFloat(), 0.5f, 0.5f)
                matrix.mapRect(outputBitmapDst)
                outputBitmapDst.left *= outputBitmapCanvas.width
                outputBitmapDst.right *= outputBitmapCanvas.width
                outputBitmapDst.top *= outputBitmapCanvas.height
                outputBitmapDst.bottom *= outputBitmapCanvas.height
            }
        }

        override fun process(
            decoder: MediaCodec,
            decoderOutputBuffer: ByteBuffer,
            decoderOutputBufferInfo: MediaCodec.BufferInfo,
            encoder: MediaCodec,
            encoderInputBufferId: Int,
            encoderInputBuffer: ByteBuffer
        ) {
            val bitmap = convertToRgb(decoderOutputBuffer, decoderOutputBufferInfo, inputYuvBuffer, inputBitmap)
            val resizedBitmap = resizeBitmap(bitmap)
            convertToYuv420sp(resizedBitmap, encoderInputBuffer, outputYuvBuffer)
            encoder.queueInputBuffer(encoderInputBufferId, 0, encoderInputBuffer.limit(),
                decoderOutputBufferInfo.presentationTimeUs, 0)
        }

        private fun resizeBitmap(bitmap: Bitmap): Bitmap {
            if (outputBitmapDst.width() == 0f && outputBitmapDst.height() == 0f) {
                // if crop params is empty, just do it automatically
                return ThumbnailUtils.extractThumbnail(bitmap, outputBitmap.width, outputBitmap.height)
            }
            outputBitmapCanvas.drawColor(Color.BLACK)
            outputBitmapCanvas.drawBitmap(bitmap, outputBitmapSrc, outputBitmapDst, outputBitmapPaint)
            return outputBitmap
        }

        private fun convertToRgb(
            byteBuffer: ByteBuffer,
            bufferInfo: MediaCodec.BufferInfo,
            inputBytes: ByteArray,
            inputBitmap: Bitmap
        ): Bitmap {
            val yuvToRgb = ScriptIntrinsicYuvToRGB.create(renderScript, Element.U8_4(renderScript));
            val inAllocation = Allocation.createSized(renderScript, Element.U8(renderScript), inputBytes.size)
            byteBuffer.position(bufferInfo.offset)
            byteBuffer.get(inputBytes)
            inAllocation.copyFrom(inputBytes)
            val outAllocation = Allocation.createFromBitmap(renderScript, inputBitmap)
            yuvToRgb.setInput(inAllocation)
            yuvToRgb.forEach(outAllocation)
            outAllocation.copyTo(inputBitmap)
            return inputBitmap
        }

        private fun convertToYuv420sp(bitmap: Bitmap, byteBuffer: ByteBuffer, outputBytes: ByteArray) {
            val rgbToYuv = ScriptC_RgbToYuv(renderScript)
            rgbToYuv._height = bitmap.height
            rgbToYuv._width = bitmap.width
            rgbToYuv._yuvAllocation = outputYuvAllocation
            val bitmapAllocation = Allocation.createFromBitmap(renderScript, bitmap)
            rgbToYuv.forEach_convert(bitmapAllocation)
            outputYuvAllocation.copyTo(outputBytes)
            byteBuffer.put(outputBytes)
        }
    }

    /** Frame was read from source, transcoded and written to result. **/
    private interface FrameCallback {

        /** [timeUs] in microseconds. **/
        fun onFrameCompleted(timeUs: Long)
    }

    class ReportingFrameCallback(
        private val callback: VideoConverter.Callback,
        private val durationUs: Long,
        private val tracksCount: Int
    ) : FrameCallback {

        private val perTrack = 1f / tracksCount
        private var trackIndex = 0

        override fun onFrameCompleted(timeUs: Long) {
            val progress = Util.constrainValue(trackIndex * perTrack + timeUs.toFloat() / durationUs * perTrack, 0f, 1f)
            callback.onProgressChanged(progress)
        }

        fun switchTrack() {
            (trackIndex + 1 >= tracksCount) illegalState "All tracks processed"
            trackIndex++
        }
    }

    private class PeekBufferIds(
        val configurationId: Int,
        val configurationInfo: MediaCodec.BufferInfo?,
        val dataId: Int,
        val dataInfo: MediaCodec.BufferInfo
    )
}
