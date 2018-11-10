package ru.cherryperry.instavideo.renderscript

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.renderscript.Allocation
import android.renderscript.Element
import android.renderscript.RenderScript
import android.renderscript.ScriptIntrinsicYuvToRGB
import android.renderscript.Type
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import ru.cherryperry.instavideo.testResources

@RunWith(Parameterized::class)
class RgbToYuvTest(
    private val assetName: String
) {

    companion object {
        @JvmStatic
        @Parameterized.Parameters(name = "{0}")
        fun data(): Collection<Array<Any>> {
            return listOf(
                arrayOf<Any>("sample_image_2x2.jpg"),
                arrayOf<Any>("sample_image_4x4.jpg"),
                arrayOf<Any>("sample_image_64x32.jpg"),
                arrayOf<Any>("sample_image_400x400.jpg")
            )
        }
    }

    @Test
    fun testConversion() {
        val bitmap = testResources().assets.open(assetName).use {
            BitmapFactory.decodeStream(it)
        }

        // convert to yuv
        val renderScript = RenderScript.create(ApplicationProvider.getApplicationContext())
        val rgbToYuv = ScriptC_RgbToYuv(renderScript)
        val yuvAllocation = Allocation.createSized(renderScript, Element.U8(renderScript), bitmap.width * bitmap.height * 3 / 2)
        rgbToYuv._height = bitmap.height
        rgbToYuv._width = bitmap.width
        rgbToYuv._yuvAllocation = yuvAllocation
        val bitmapAllocation = Allocation.createFromBitmap(renderScript, bitmap)
        rgbToYuv.forEach_convert(bitmapAllocation)

        // result to by array
        val byteArray = ByteArray(yuvAllocation.bytesSize)
        yuvAllocation.copyTo(byteArray)

        // compare with valid kotlin implementation
        val kotlinImplementation = RgbToYuvJvmImplementation().convertToYuv(bitmap)
        Assert.assertArrayEquals(kotlinImplementation, byteArray)

        // convert back to rgb
        val yuvToRgb = ScriptIntrinsicYuvToRGB.create(renderScript, Element.U8_4(renderScript))
        val yuvType = Type.Builder(renderScript, Element.U8(renderScript)).setX(byteArray.size)
        val inData = Allocation.createTyped(renderScript, yuvType.create(), Allocation.USAGE_SCRIPT)
        val rgbaType = Type.Builder(renderScript, Element.RGBA_8888(renderScript)).setX(bitmap.width).setY(bitmap.height)
        val outData = Allocation.createTyped(renderScript, rgbaType.create(), Allocation.USAGE_SCRIPT)
        inData.copyFrom(byteArray)
        yuvToRgb.setInput(inData)
        yuvToRgb.forEach(outData)

        val decodedBitmap = Bitmap.createBitmap(bitmap.width, bitmap.height, Bitmap.Config.ARGB_8888)
        outData.copyTo(decodedBitmap)

        // check bitmap is valid in debugger!
        // color data is lost after conversion especially on small images
    }
}
