package ru.cherryperry.instavideo.data.media.conversion.frame

import android.media.MediaCodec
import android.media.MediaFormat
import java.nio.ByteBuffer

class FrameData(
    val byteBuffer: ByteBuffer,
    val bufferInfo: MediaCodec.BufferInfo,
    val mediaFormat: MediaFormat
) {

    val width = mediaFormat.getInteger(MediaFormat.KEY_WIDTH)
    val height = mediaFormat.getInteger(MediaFormat.KEY_HEIGHT)

    constructor(byteBuffer: ByteBuffer, mediaFormat: MediaFormat) : this(
        byteBuffer,
        MediaCodec.BufferInfo().apply {
            size = byteBuffer.limit()
            offset = byteBuffer.position()
        },
        mediaFormat
    )

    fun offsetPosition() {
        byteBuffer.position(bufferInfo.offset)
    }
}
