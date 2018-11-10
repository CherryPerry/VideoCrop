package ru.cherryperry.instavideo.renderscript

import android.graphics.Bitmap
import android.graphics.Color

/**
 * Compares two bitmaps with color loss after YUV <-> RGB conversion.
 */
object BitmapComparator {

    /**
     * Images are very similiar.
     */
    fun areImagesProbablySame(bitmap1: Bitmap, bitmap2: Bitmap): Boolean {
        if (bitmap1.width != bitmap2.width || bitmap1.height != bitmap2.height || bitmap1.config != bitmap2.config) {
            return false
        }
        for (y in 0 until bitmap1.height) {
            for (x in 0 until bitmap1.width) {
                val p1 = bitmap1.getPixel(x, y)
                val p2 = bitmap2.getPixel(x, y)
                if (!(p1.r() colorDelta p2.r() && p1.g() colorDelta p2.g() && p1.b() colorDelta p2.b())) {
                    return false
                }
            }
        }
        return true
    }

    /** 20% color delta **/
    private infix fun Int.colorDelta(other: Int): Boolean = Math.abs(this - other) < 50

    /** Red color component **/
    private fun Int.r(): Int = Color.red(this)

    /** Green color component **/
    private fun Int.g(): Int = Color.green(this)

    /** Blue color component **/
    private fun Int.b(): Int = Color.blue(this)
}
