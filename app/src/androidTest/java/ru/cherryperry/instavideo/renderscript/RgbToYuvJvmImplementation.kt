package ru.cherryperry.instavideo.renderscript

import android.graphics.Bitmap

/**
 * Valid implementation of conversion. Renderscript implementation is based on this algorithm.
 *
 * [https://stackoverflow.com/questions/5960247/convert-bitmap-array-to-yuv-ycbcr-nv21]
 */
class RgbToYuvJvmImplementation {

    fun convertToYuv(bitmap: Bitmap): ByteArray {
        val argb = IntArray(bitmap.width * bitmap.height)
        bitmap.getPixels(argb, 0, bitmap.width, 0, 0, bitmap.width, bitmap.height)
        val yuv = ByteArray(bitmap.width * bitmap.height * 3 / 2)
        encodeYUV420SP(yuv, argb, bitmap.width, bitmap.height)
        return yuv
    }

    private fun encodeYUV420SP(yuv420sp: ByteArray, argb: IntArray, width: Int, height: Int) {
        val frameSize = width * height
        var yIndex = 0
        var uvIndex = frameSize
        var index = 0
        for (j in 0 until height) {
            for (i in 0 until width) {
                val r = argb[index] and 0xff0000 shr 16
                val g = argb[index] and 0xff00 shr 8
                val b = argb[index] and 0xff shr 0
                // well known RGB to YUV algorithm
                val y = (66 * r + 129 * g + 25 * b + 128 shr 8) + 16
                val u = (-38 * r - 74 * g + 112 * b + 128 shr 8) + 128
                val v = (112 * r - 94 * g - 18 * b + 128 shr 8) + 128
                /*
                NV21 has a plane of Y and interleaved planes of VU each sampled by a factor of 2
                meaning for every 4 Y pixels there are 1 V and 1 U. Note the sampling is every other
                pixel AND every other scanline.
                */
                yuv420sp[yIndex++] = (if (y < 0) 0 else if (y > 255) 255 else y).toByte()
                if (j % 2 == 0 && index % 2 == 0) {
                    yuv420sp[uvIndex++] = (if (v < 0) 0 else if (v > 255) 255 else v).toByte()
                    yuv420sp[uvIndex++] = (if (u < 0) 0 else if (u > 255) 255 else u).toByte()
                }
                index++
            }
        }
    }
}
