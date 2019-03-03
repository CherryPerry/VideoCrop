package ru.cherryperry.instavideo.data.media.conversion.async

import android.media.MediaCodec
import android.media.MediaExtractor
import android.media.MediaFormat
import android.os.Handler
import android.os.Looper
import io.reactivex.functions.Consumer
import ru.cherryperry.instavideo.data.media.conversion.MediaExtractorSource
import ru.cherryperry.instavideo.data.media.extractor.MediaExtractorData
import ru.cherryperry.instavideo.data.media.extractor.selectTrack
import timber.log.Timber
import java.io.Closeable
import java.util.concurrent.CountDownLatch
import java.util.concurrent.atomic.AtomicInteger

class DecoderMediaCallback(
    codecHolder: CodecHolder,
    private val source: MediaExtractorSource,
    private val data: MediaExtractorData,
    private val startUs: Long,
    private val endUs: Long,
    private val bufferConsumer: Consumer<CloseableCodecOutputBuffer>
) : CodecHolderCallback(codecHolder), Closeable {

    private val finishLatch = CountDownLatch(1)
    private val submittedBufferCounter = AtomicInteger(0)
    private val decodedBufferCounter = AtomicInteger(0)
    private val handler by lazy(LazyThreadSafetyMode.NONE) { Handler(Looper.myLooper()) }
    private val mediaExtractor by lazy(LazyThreadSafetyMode.NONE) {
        Timber.d("Initialize MediaExtractor with track number %d and start time %d", data.index, startUs)
        val mediaExtractor = MediaExtractor()
        source.setDataSource(mediaExtractor)
        mediaExtractor.selectTrack(data)
        mediaExtractor.seekTo(startUs, MediaExtractor.SEEK_TO_PREVIOUS_SYNC)
        mediaExtractor
    }
    private var lastPresentationTime: Long = 0

    override fun onInputBufferAvailable(codec: CodecHolder, index: Int) {
        Timber.d("onInputBufferAvailable: index %d", index)
        val mediaExtractor = mediaExtractor
        if (mediaExtractor.sampleTime > endUs) {
            Timber.d("Sample time is after end time, send EoF")
            codec.queueEndOfStream(index)
            return
        }
        val buffer = codec.getInputBuffer(index)
        val read = mediaExtractor.readSampleData(buffer, 0)
        if (read <= 0) {
            Timber.d("Nothing to read, send EoF")
            codec.queueEndOfStream(index)
            return
        }
        val submittedCount = submittedBufferCounter.incrementAndGet()
        val eof = if (mediaExtractor.advance()) 0 else MediaCodec.BUFFER_FLAG_END_OF_STREAM
        codec.queueInputBuffer(index, 0, read, mediaExtractor.sampleTime, mediaExtractor.sampleFlags or eof)
        Timber.d("Submitted buffer index %d", submittedCount)
    }

    override fun onOutputBufferAvailable(codec: CodecHolder, index: Int, info: MediaCodec.BufferInfo) {
        Timber.d("onOutputBufferAvailable: index %d time %d", index, info.presentationTimeUs)
        val decodedCount = decodedBufferCounter.incrementAndGet()
        if (info.presentationTimeUs in startUs..endUs || info.size == 0 && info.isEndOfStream()) {
            val buffer = codec.getOutputBuffer(index)
            val data = CloseableCodecOutputBuffer(codec, buffer, info, index,
                CloseableCodecOutputBuffer.Debug(data.index, decodedCount), handler)
            Timber.d("Transfered buffer %s", data.debug)
            bufferConsumer.accept(data)
            if (info.presentationTimeUs <= lastPresentationTime) {
                Timber.e("onOutputBufferAvailable is not sequent!")
                info.presentationTimeUs = lastPresentationTime
            }
            lastPresentationTime = info.presentationTimeUs
        } else {
            Timber.d("Drop frame, it is not in time bounds")
            if (info.isEndOfStream()) {
                Timber.e("Dropped end of stream!")
            }
            codec.releaseOutputBuffer(index)
        }
        Timber.d("Decoded buffer index %d", decodedCount)
        if (decodedCount == submittedBufferCounter.get() && codec.currentState != CodecHolder.State.Running) {
            Timber.d("All buffers are processed")
            finishLatch.countDown()
        }
    }

    override fun onOutputFormatChanged(codec: CodecHolder, format: MediaFormat) {
        Timber.d("onOutputFormatChanged: format %s", format)
    }

    override fun onError(codec: CodecHolder, exception: MediaCodec.CodecException) {
        Timber.e(exception, "onError")
        finishLatch.countDown()
    }

    override fun close() {
        mediaExtractor.release()
    }

    fun await() {
        Timber.d("Start await")
        finishLatch.await()
        Timber.d("End await")
    }
}
