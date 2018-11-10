package ru.cherryperry.instavideo.data.media.codec

import android.media.MediaCodec
import java.nio.ByteBuffer

/** Wait for [MediaCodec.dequeueInputBuffer] until ready. */
fun MediaCodec.dequeueInputBufferInfinite() = this.dequeueInputBuffer(-1)

/** Wait for [MediaCodec.dequeueOutputBuffer] until ready. */
fun MediaCodec.dequeueOutputBufferInfinite(bufferInfo: MediaCodec.BufferInfo) = this.dequeueOutputBuffer(bufferInfo, -1)

/**
 * Dequeue output buffer and pass it with it's id to [block].
 * Releases buffer after [block] invocation.
 */
fun <T> MediaCodec.withOutputBuffer(bufferInfo: MediaCodec.BufferInfo, block: (Int, ByteBuffer) -> T): T {
    val bufferId = this.dequeueOutputBuffer(bufferInfo, -1)
    if (bufferId < 0) {
        // all negative values are "try again"
        return this.withOutputBuffer(bufferInfo, block)
    }
    val buffer: ByteBuffer = this.getOutputBuffer(bufferId)!!
    try {
        return block(bufferId, buffer)
    } finally {
        this.releaseOutputBuffer(bufferId, false)
    }
}

/**
 * Dequeue input buffer and pass it with it's id to [block].
 * Caller must call [MediaCodec.queueInputBuffer] in [block]!
 */
fun <T> MediaCodec.withInputBuffer(block: (Int, ByteBuffer) -> T): T {
    val bufferId = this.dequeueInputBuffer(-1)
    val buffer: ByteBuffer = this.getInputBuffer(bufferId)!!
    return block(bufferId, buffer)
}
