package ru.cherryperry.instavideo.data.media.conversion

import android.content.Context
import android.graphics.RectF
import android.media.MediaExtractor
import android.media.MediaFormat
import android.media.MediaMuxer
import android.os.Handler
import android.os.HandlerThread
import android.os.Looper
import com.google.android.exoplayer2.util.Util
import ru.cherryperry.instavideo.core.illegalArgument
import ru.cherryperry.instavideo.data.media.codec.MediaCodecFactory
import ru.cherryperry.instavideo.data.media.conversion.async.DecoderMediaCallback
import ru.cherryperry.instavideo.data.media.conversion.async.EncoderMediaCallback
import ru.cherryperry.instavideo.data.media.conversion.async.MediaSink
import ru.cherryperry.instavideo.data.media.conversion.frame.DefaultRawFrameProcessor
import ru.cherryperry.instavideo.data.media.conversion.frame.VideoTransformationRawFrameProcessor
import ru.cherryperry.instavideo.data.media.extractor.MediaExtractorData
import ru.cherryperry.instavideo.data.media.extractor.getTrackData
import ru.cherryperry.instavideo.data.media.extractor.use
import ru.cherryperry.instavideo.data.media.format.getIntegerDefault
import timber.log.Timber
import java.util.concurrent.CountDownLatch
import javax.inject.Inject
import kotlin.math.max
import kotlin.math.min

class AsyncVideoConverterImpl @Inject constructor(
    private val context: Context
) : VideoConverter {

    private val videoDecoderThread = HandlerThread("VideoDecoderThread")
    private val videoEncoderThread = HandlerThread("VideoEncoderThread")
    private val audioDecoderThread = HandlerThread("AudioDecoderThread")
    private val audioEncoderThread = HandlerThread("AudioEncoderThread")
    private val threads = arrayOf(videoDecoderThread, videoEncoderThread, audioDecoderThread, audioEncoderThread)

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
        val (videoTrack, audioTrack) = MediaExtractor().use {
            source.setDataSource(this)
            selectTracks(this)
        }
        try {
            threads.forEach { it.start() }
            // TODO Audio!
            transcodeAndMux(videoTrack, null, startUs, endUs, sourceRect, outputFile, callback, source)
        } catch (exception: Exception) {
            Timber.e(exception)
            throw exception
        }
    }

    /** Read track's info from [MediaExtractor]. First one is video, second is audio. **/
    private fun selectTracks(mediaExtractor: MediaExtractor): Pair<MediaExtractorData, MediaExtractorData?> {
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
        return videoData to audioData
    }

    private fun transcodeAndMux(
        videoData: MediaExtractorData,
        audioData: MediaExtractorData?,
        startUs: Long,
        endUs: Long,
        sourceRect: RectF,
        outputFile: String,
        callback: VideoConverter.Callback,
        source: MediaExtractorSource
    ) {
        // peek video frame for additional data in output format
        val videoDecoder = MediaCodecFactory.createDecoderAndConfigure(videoData.mediaFormat)
        val videoFps = videoData.mediaFormat.getIntegerDefault(MediaFormat.KEY_FRAME_RATE, 0)
        val videoEncoder = MediaCodecFactory.createVideoEncoder(videoFps)
        val rotation = if (videoData.mediaFormat.containsKey(MediaFormat.KEY_ROTATION)) {
            videoData.mediaFormat.getInteger(MediaFormat.KEY_ROTATION)
        } else {
            0
        }
        val videoFrameProcessor = VideoTransformationRawFrameProcessor(context, sourceRect, rotation)

        // peek audio frame for additional data in output format
        val audioDecoder = if (audioData != null) {
            MediaCodecFactory.createDecoderAndConfigure(audioData.mediaFormat)
        } else {
            null
        }
        val audioEncoder = if (audioDecoder != null && audioData != null) {
            val sampleRate = audioData.mediaFormat.getInteger(MediaFormat.KEY_SAMPLE_RATE)
            val channelCount = audioData.mediaFormat.getInteger(MediaFormat.KEY_CHANNEL_COUNT)
            MediaCodecFactory.createAudioEncoder(sampleRate, channelCount)
        } else {
            null
        }
        val audioFrameProcessor = DefaultRawFrameProcessor()

        // start transcoding
        val mediaMuxer = MediaMuxer(outputFile, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4)
        val mediaSink = MediaSink(mediaMuxer, if (audioData != null) 2 else 1)

        val reportingFrameCallback = ReportingFrameCallback(
            callback,
            min(
                endUs - startUs,
                videoData.mediaFormat.getLong(MediaFormat.KEY_DURATION)
            ),
            if (audioData != null) 2 else 1)

        val videoEncoderCallback = EncoderMediaCallback(
            videoEncoder,
            mediaSink,
            videoFrameProcessor,
            Handler(videoEncoderThread.looper),
            { reportingFrameCallback.onFrameCompleted(it, 0) }
        )
        val videoEncoderHandler = Handler(videoEncoderThread.looper)
        videoEncoderHandler.postAndWait {
            videoEncoder.configure(videoEncoderCallback, videoEncoderHandler)
        }
        val videoDecoderCallback = DecoderMediaCallback(
            videoDecoder, source, videoData, startUs, endUs, videoEncoderCallback
        )
        val videoDecoderHandler = Handler(videoDecoderThread.looper)
        videoDecoderHandler.postAndWait {
            videoDecoder.configure(videoDecoderCallback, videoDecoderHandler)
        }

        videoEncoder.start()
        videoDecoder.start()

        if (audioData != null) {
            val audioEncoderHandler = Handler(audioEncoderThread.looper)
            val audioEncoderCallback = EncoderMediaCallback(
                audioEncoder!!,
                mediaSink,
                audioFrameProcessor,
                audioEncoderHandler,
                { reportingFrameCallback.onFrameCompleted(it, 1) }
            )
            audioEncoderHandler.postAndWait {
                audioEncoder.configure(audioEncoderCallback, audioEncoderHandler)
            }
            val audioDecoderCallback = DecoderMediaCallback(
                audioDecoder!!, source, audioData, startUs, endUs, videoEncoderCallback
            )
            val audioDecoderHandler = Handler(audioDecoderThread.looper)
            audioDecoderHandler.postAndWait {
                audioDecoder.configure(audioDecoderCallback, audioDecoderHandler)
            }

            audioEncoder.start()

            audioDecoder.start()

            audioDecoderCallback.await()
            audioEncoderCallback.await()

            audioDecoder.stop()
            audioDecoder.release()

            audioEncoder.stop()
            audioEncoder.release()

            audioDecoderCallback.close()
            audioEncoderCallback.close()
        }

        videoDecoderCallback.await()
        videoEncoderCallback.await()

        videoDecoder.stop()
        videoDecoder.release()

        videoEncoder.stop()
        videoEncoder.release()

        videoDecoderCallback.close()
        videoEncoderCallback.close()

        videoFrameProcessor.close()

        mediaMuxer.release()
    }

    class ReportingFrameCallback(
        private val callback: VideoConverter.Callback,
        private val durationUs: Long,
        private val tracksCount: Int
    ) {

        private val perTrack = 1f / tracksCount
        private val progresses = Array<Float>(tracksCount) { 0f }

        fun onFrameCompleted(timeUs: Long, trackIndex: Int) {
            progresses[trackIndex] = max(
                progresses[trackIndex],
                Util.constrainValue(timeUs.toFloat() / durationUs * perTrack, 0f, 1f)
            )
            callback.onProgressChanged(progresses.sum() / tracksCount)
        }
    }

    override fun close() {
        threads.forEach { it.quitSafely() }
    }

    private inline fun Handler.postAndWait(crossinline block: () -> Unit) {
        if (Looper.myLooper() == looper) {
            block()
        } else {
            val countDownLatch = CountDownLatch(1)
            post {
                block()
                countDownLatch.countDown()
            }
            countDownLatch.await()
        }
    }
}
