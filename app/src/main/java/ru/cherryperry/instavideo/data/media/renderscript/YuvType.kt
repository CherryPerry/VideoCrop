package ru.cherryperry.instavideo.data.media.renderscript

/**
 * YUV types with id corresponding to [ru.cherryperry.instavideo.renderscript.ScriptC_YuvToRgb].
 */
enum class YuvType(
    val id: Int
) {
    YUV420Planar(0),
    YUV420Packed(1),
    YUV420SemiPlanarNV12(2),
    YUV420SemiPlanarNV21(3)
}
