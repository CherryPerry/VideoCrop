package ru.cherryperry.instavideo.data.media.conversion.async

import android.media.MediaCodec
import android.os.Handler
import timber.log.Timber
import java.io.Closeable
import java.nio.ByteBuffer

class CloseableCodecOutputBuffer(
    private val codecHolder: CodecHolder,
    val byteBuffer: ByteBuffer,
    val bufferInfo: MediaCodec.BufferInfo,
    private val index: Int,
    val debug: Debug,
    private val hander: Handler
) : Closeable {

    val mediaFormat = codecHolder.outputFormat

    init {
        Timber.d("Create buffer %s", debug)
    }

    override fun close() {
        Timber.d("Close buffer %s", debug)
        hander.post {
            try {
                codecHolder.releaseOutputBuffer(index)
            } catch (illegalStateException: IllegalStateException) {
                // don't care about it, if codec is already closed
            }
        }
    }

    data class Debug(
        val trackIndex: Int,
        val frameIndex: Int
    )
}
