package ru.cherryperry.instavideo.domain.editor

import ru.cherryperry.instavideo.core.illegalArgument

/** Video file information (width, height and duration). **/
data class VideoFileMetaData(
    val width: Long,
    val height: Long,
    val durationMs: Long
) {

    init {
        (width <= 0) illegalArgument "Width can't be negative or zero"
        (height <= 0) illegalArgument "Height can't be negative or zero"
        (durationMs <= 0) illegalArgument "Duration can't be negative or zero"
    }
}
