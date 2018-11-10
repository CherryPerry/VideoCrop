package ru.cherryperry.instavideo.presentation.util

import android.view.View

val View.centerX
    get() = left + width / 2

val View.centerY
    get() = top + height / 2

val View.scaledWidth
    get() = width * scaleX

val View.scaledHeight
    get() = height * scaleY

val View.scaledLeft
    get() = centerX - scaledWidth / 2

val View.scaledRight
    get() = centerX + scaledWidth / 2

val View.scaledTop
    get() = centerY - scaledHeight / 2

val View.scaledBottom
    get() = centerY + scaledHeight / 2

var View.gone: Boolean
    get() = visibility == View.GONE
    set(value) {
        visibility = if (value) View.GONE else View.VISIBLE
    }
