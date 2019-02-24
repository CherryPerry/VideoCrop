package ru.cherryperry.instavideo.data.media.conversion.async

import android.media.MediaCodec

internal fun MediaCodec.BufferInfo.isEndOfStream() = flags.isEndOfStream()

internal fun Int.isEndOfStream() = this and MediaCodec.BUFFER_FLAG_END_OF_STREAM != 0
