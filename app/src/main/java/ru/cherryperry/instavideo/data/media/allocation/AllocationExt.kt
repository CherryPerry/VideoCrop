package ru.cherryperry.instavideo.data.media.allocation

import android.os.Build
import android.renderscript.Allocation
import java.nio.ByteBuffer

/**
 * Copies content of [byteBuffer] to this [Allocation].
 *
 * [Allocation] does not support read-write to [ByteBuffer] directly.
 * But starting from [Build.VERSION_CODES.N] it can be done through [Allocation.getByteBuffer].
 */
fun Allocation.copyFromByteBuffer(byteBuffer: ByteBuffer) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
        // direct write from byte buffer
        this.byteBuffer.apply {
            position(0)
            put(byteBuffer)
        }
    } else {
        // allocate and create new array every time
        val array = ByteArray(byteBuffer.remaining())
        byteBuffer.get(array)
        copyFrom(array)
    }
}

/**
 * Copies content of [Allocation] to provided [byteBuffer].
 *
 * [Allocation] does not support read-write to [ByteBuffer] directly.
 * But starting from [Build.VERSION_CODES.N] it can be done through [Allocation.getByteBuffer].
 */
fun Allocation.copyToByteBuffer(byteBuffer: ByteBuffer) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
        // direct write from byte buffer
        this.byteBuffer.apply {
            position(0)
            byteBuffer.put(this)
        }
    } else {
        // allocate and create new array every time
        val array = ByteArray(bytesSize)
        copyTo(array)
        byteBuffer.put(array)
    }
}
