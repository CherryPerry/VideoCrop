package ru.cherryperry.instavideo.renderscript

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.renderscript.Allocation
import android.renderscript.Element
import android.renderscript.RenderScript
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import ru.cherryperry.instavideo.data.media.renderscript.ScriptC_RgbToYuv
import ru.cherryperry.instavideo.data.media.renderscript.ScriptC_YuvToRgb
import ru.cherryperry.instavideo.data.media.renderscript.YuvType
import ru.cherryperry.instavideo.data.media.renderscript.yuvAllocationSize
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
                //arrayOf<Any>("sample_image_2x2.jpg"),
                //arrayOf<Any>("sample_image_4x4.jpg"),
                //arrayOf<Any>("sample_image_64x32.jpg"),
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
        val yuvAllocation = Allocation.createSized(renderScript, Element.U8(renderScript), yuvAllocationSize(bitmap.width, bitmap.height))
        rgbToYuv._yuv = yuvAllocation
        rgbToYuv._height = bitmap.height
        rgbToYuv._width = bitmap.width
        rgbToYuv._type = YuvType.YUV420SemiPlanarNV21.id
        val bitmapAllocation = Allocation.createFromBitmap(renderScript, bitmap)
        rgbToYuv.forEach_convert(bitmapAllocation)

        // result to by array
        val byteArray = ByteArray(yuvAllocation.bytesSize)
        yuvAllocation.copyTo(byteArray)

        // compare with valid kotlin implementation
        val kotlinImplementation = RgbToYuvJvmImplementation().convertToYuv(bitmap)
        Assert.assertArrayEquals(kotlinImplementation, byteArray)

        // convert back to rgb

        val decodedBitmap = Bitmap.createBitmap(bitmap.width, bitmap.height, Bitmap.Config.ARGB_8888)
        val outData = Allocation.createFromBitmap(renderScript, decodedBitmap)

        val yuvToRgb = ScriptC_YuvToRgb(renderScript)
        yuvToRgb._yuv = yuvAllocation
        yuvToRgb._height = bitmap.height
        yuvToRgb._width = bitmap.width
        yuvToRgb._type = YuvType.YUV420SemiPlanarNV21.id
        yuvToRgb.forEach_convert(outData)

        /*val yuvToRgb = ScriptIntrinsicYuvToRGB.create(renderScript, Element.U8_4(renderScript))
        yuvToRgb.setInput(yuvAllocation)
        yuvToRgb.forEach(outData)*/

        outData.copyTo(decodedBitmap)

        // check bitmap is valid in debugger!
        // color data is lost after conversion especially on small images
        val breakPointMe = decodedBitmap.byteCount
    }
}
