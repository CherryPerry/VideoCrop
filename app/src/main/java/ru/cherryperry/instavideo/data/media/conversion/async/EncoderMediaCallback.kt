package ru.cherryperry.instavideo.data.media.conversion.async

import android.media.MediaCodec
import android.media.MediaFormat
import android.os.Handler
import io.reactivex.functions.Consumer
import ru.cherryperry.instavideo.data.media.conversion.frame.FrameData
import ru.cherryperry.instavideo.data.media.conversion.frame.RawFrameProcessor
import timber.log.Timber
import java.io.Closeable
import java.util.concurrent.CountDownLatch
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger

class EncoderMediaCallback(
    codecHolder: CodecHolder,
    private val mediaSink: MediaSink,
    private val frameProcessor: RawFrameProcessor,
    private val handler: Handler,
    private val frameCompleteCallback: (Long) -> Unit
) : CodecHolderCallback(codecHolder), Consumer<CloseableCodecOutputBuffer>, Closeable {

    private val queue = LinkedBlockingQueue<CloseableCodecOutputBuffer>()
    private val countDownLatch = CountDownLatch(1)
    private val outputBufferCounter = AtomicInteger(0)

    override fun onInputBufferAvailable(codec: CodecHolder, index: Int) {
        Timber.d("onInputBufferAvailable: index %d", index)
        queue.poll(Long.MAX_VALUE, TimeUnit.NANOSECONDS)?.let {
            Timber.d("Receive buffer: size %d time %d debug %s",
                it.bufferInfo.size, it.bufferInfo.presentationTimeUs, it.debug)
            val byteBuffer = codec.getInputBuffer(index)
            if (it.bufferInfo.size > 0) {
                frameProcessor.process(
                    FrameData(it.byteBuffer, it.bufferInfo, it.mediaFormat),
                    FrameData(byteBuffer, codec.outputFormat)
                )
            }
            codec.queueInputBuffer(index, 0, byteBuffer.limit(), it.bufferInfo.presentationTimeUs, it.bufferInfo.flags)
            it.close()
        }
    }

    override fun onOutputBufferAvailable(codec: CodecHolder, index: Int, info: MediaCodec.BufferInfo) {
        Timber.d("onOutputBufferAvailable: index %d, counter %d", index, outputBufferCounter.getAndIncrement())
        if (mediaSink.applyCodec(codec.outputFormat)) {
            Timber.d("Codec is applied")
        }
        Timber.d("onOutputBufferAvailable: time %d size %d", info.presentationTimeUs, info.size)
        if (info.size > 0) {
            val buffer = codec.getOutputBuffer(index)
            Timber.d("Write encoded data to file")
            mediaSink.sink(codec.outputFormat, buffer, info)
            frameCompleteCallback(info.presentationTimeUs)
        }
        codec.releaseOutputBuffer(index)
        if (info.isEndOfStream()) {
            countDownLatch.countDown()
        }
    }

    override fun onOutputFormatChanged(codec: CodecHolder, format: MediaFormat) {
        Timber.d("onOutputFormatChanged: format %s", format)
    }

    override fun onError(codec: CodecHolder, exception: MediaCodec.CodecException) {
        Timber.e(exception, "onError")
        countDownLatch.countDown()
    }

    override fun close() {
        // no-op
    }

    override fun accept(data: CloseableCodecOutputBuffer) {
        queue.add(data)
    }

    fun await() {
        Timber.d("Start await")
        countDownLatch.await()
        Timber.d("End await")
    }
}
