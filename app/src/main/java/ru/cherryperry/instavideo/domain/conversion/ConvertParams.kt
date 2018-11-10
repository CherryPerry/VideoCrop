package ru.cherryperry.instavideo.domain.conversion

import android.graphics.RectF
import android.net.Uri
import ru.cherryperry.instavideo.core.illegalArgument

data class ConvertParams(
    val sourceUri: Uri,
    val targetUri: Uri,
    val startUs: Long,
    val endUs: Long,
    val sourceRect: RectF
) {

    init {
        (startUs > endUs) illegalArgument "Start can't be later than end"
    }
}
