package ru.cherryperry.instavideo.data.media.renderscript

import android.media.MediaCodecInfo
import android.media.MediaFormat
import ru.cherryperry.instavideo.core.illegalArgument

fun yuvAllocationSize(width: Int, height: Int): Int {
    (width <= 0) illegalArgument "Width can't be negative or zero"
    (height <= 0) illegalArgument "Height can't be negative or zero"
    return width * height * 3 / 2
}

fun MediaFormat.toYuvType() =
    if (this.containsKey(MediaFormat.KEY_COLOR_FORMAT)) {
        when (this.getInteger(MediaFormat.KEY_COLOR_FORMAT)) {
            MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420SemiPlanar -> YuvType.YUV420SemiPlanarNV21
            MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420Planar -> YuvType.YUV420Planar
            else -> throw IllegalArgumentException("Unsupported color format")
        }
    } else {
        null
    }
