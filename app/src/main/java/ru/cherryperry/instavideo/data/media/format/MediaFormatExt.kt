package ru.cherryperry.instavideo.data.media.format

import android.media.MediaFormat

fun MediaFormat.getIntegerDefault(key: String, default: Int) =
    if (containsKey(key)) getInteger(key) else default
