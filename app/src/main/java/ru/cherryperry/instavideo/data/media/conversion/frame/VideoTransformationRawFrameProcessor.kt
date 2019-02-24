package ru.cherryperry.instavideo.data.media.conversion.frame

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
import android.media.ThumbnailUtils
import android.os.Build
import android.renderscript.Allocation
import android.renderscript.Element
import android.renderscript.RenderScript
import ru.cherryperry.instavideo.BuildConfig
import ru.cherryperry.instavideo.data.media.allocation.copyFromByteBuffer
import ru.cherryperry.instavideo.data.media.allocation.copyToByteBuffer
import ru.cherryperry.instavideo.data.media.renderscript.ScriptC_RgbToYuv
import ru.cherryperry.instavideo.data.media.renderscript.ScriptC_YuvToRgb
import ru.cherryperry.instavideo.data.media.renderscript.YuvType
import ru.cherryperry.instavideo.data.media.renderscript.toYuvType
import ru.cherryperry.instavideo.data.media.renderscript.yuvAllocationSize
import java.io.Closeable

/**
 * Resize and crop frame from decoder's size to encoder's.
 * YUV operations are hard to implement, so convert it to RGB first, and make it easy with [Bitmap].
 * NOT thread safe.
 */
class VideoTransformationRawFrameProcessor(
    private val context: Context,
    sourceRect: RectF,
    private val rotation: Int
) : RawFrameProcessor, Closeable {

    private val outputBitmapPaint = Paint()
    private val outputBitmapDst = RectF(sourceRect)

    private var initialized = false

    // resuable buffers
    private lateinit var renderScript: RenderScript
    private lateinit var yuvToRgb: ScriptC_YuvToRgb
    private lateinit var rgbToYuv: ScriptC_RgbToYuv
    private lateinit var inputYuvAllocation: Allocation
    private lateinit var inputBitmap: Bitmap
    private lateinit var inputBitmapAllocation: Allocation
    private lateinit var outputBitmap: Bitmap
    private lateinit var outputBitmapCanvas: Canvas
    private lateinit var outputBitmapSrc: Rect
    private lateinit var outputYuvAllocation: Allocation
    private lateinit var outputBitmapAllocation: Allocation

    override fun process(input: FrameData, output: FrameData) {
        init(input, output)
        convertToRgb(input)
        resizeBitmap()
        convertToYuv(input, output)
    }

    private fun init(input: FrameData, output: FrameData) {
        if (initialized) return
        renderScript = RenderScript.create(
            context,
            if (BuildConfig.DEBUG) RenderScript.ContextType.DEBUG else RenderScript.ContextType.NORMAL,
            RenderScript.CREATE_FLAG_NONE
        )
        yuvToRgb = ScriptC_YuvToRgb(renderScript)
        rgbToYuv = ScriptC_RgbToYuv(renderScript)
        val inputYuvSize = yuvAllocationSize(input.width, input.height)
        inputYuvAllocation = Allocation.createSized(renderScript, Element.U8(renderScript), inputYuvSize)
        inputBitmap = Bitmap.createBitmap(input.width, input.height, Bitmap.Config.ARGB_8888)
        inputBitmapAllocation = Allocation.createFromBitmap(renderScript, inputBitmap)
        outputBitmap = Bitmap.createBitmap(output.width, output.height, Bitmap.Config.ARGB_8888)
        outputBitmapCanvas = Canvas(outputBitmap)
        outputBitmapSrc = Rect(0, 0, inputBitmap.width, inputBitmap.height)
        val outputYuvSize = yuvAllocationSize(output.width, output.height)
        outputYuvAllocation = Allocation.createSized(renderScript, Element.U8(renderScript), outputYuvSize)
        outputBitmapAllocation = Allocation.createFromBitmap(renderScript, outputBitmap)
        if (outputBitmapDst.width() != 0f && outputBitmapDst.height() != 0f) {
            val matrix = Matrix()
            matrix.setRotate(-rotation.toFloat(), 0.5f, 0.5f)
            matrix.mapRect(outputBitmapDst)
            outputBitmapDst.left *= outputBitmapCanvas.width
            outputBitmapDst.right *= outputBitmapCanvas.width
            outputBitmapDst.top *= outputBitmapCanvas.height
            outputBitmapDst.bottom *= outputBitmapCanvas.height
        }
        initialized = true
    }

    private fun resizeBitmap() {
        outputBitmapCanvas.drawColor(Color.BLACK)
        if (outputBitmapDst.width() == 0f && outputBitmapDst.height() == 0f) {
            // if crop params are empty, just do it automatically
            val bitmap = ThumbnailUtils.extractThumbnail(inputBitmap, outputBitmap.width, outputBitmap.height)
            outputBitmapCanvas.drawBitmap(bitmap, 0f, 0f, outputBitmapPaint)
        } else {
            outputBitmapCanvas.drawBitmap(inputBitmap, outputBitmapSrc, outputBitmapDst, outputBitmapPaint)
        }
    }

    private fun convertToRgb(input: FrameData) {
        input.offsetPosition()
        inputYuvAllocation.copyFromByteBuffer(input.byteBuffer)
        yuvToRgb._height = inputBitmap.height
        yuvToRgb._width = inputBitmap.width
        yuvToRgb._yuv = inputYuvAllocation
        yuvToRgb._type = input.mediaFormat.toYuvType()?.id ?: YuvType.YUV420SemiPlanarNV21.id
        yuvToRgb.forEach_convert(inputBitmapAllocation)
        inputBitmapAllocation.copyTo(inputBitmap)
    }

    private fun convertToYuv(input: FrameData, output: FrameData) {
        rgbToYuv._height = outputBitmap.height
        rgbToYuv._width = outputBitmap.width
        rgbToYuv._yuv = outputYuvAllocation
        rgbToYuv._type = output.mediaFormat.toYuvType()?.id
            ?: input.mediaFormat.toYuvType()?.id
                ?: YuvType.YUV420SemiPlanarNV21.id
        outputBitmapAllocation.copyFrom(outputBitmap)
        rgbToYuv.forEach_convert(outputBitmapAllocation)
        output.byteBuffer.position(0)
        outputYuvAllocation.copyToByteBuffer(output.byteBuffer)
    }

    override fun close() {
        if (initialized) {
            yuvToRgb.destroy()
            rgbToYuv.destroy()
            inputYuvAllocation.destroy()
            inputBitmapAllocation.destroy()
            outputYuvAllocation.destroy()
            outputBitmapAllocation.destroy()
        }
        // RenderScript is reused on API >= M
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            renderScript.destroy()
        }
    }
}
