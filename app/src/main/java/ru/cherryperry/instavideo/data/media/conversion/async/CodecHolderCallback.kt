package ru.cherryperry.instavideo.data.media.conversion.async

import android.media.MediaCodec
import android.media.MediaFormat

abstract class CodecHolderCallback(
    private val codecHolder: CodecHolder
) : MediaCodec.Callback() {

    final override fun onInputBufferAvailable(codec: MediaCodec, index: Int) {
        if (codecHolder.currentState.validInputs) {
            onInputBufferAvailable(codecHolder, index)
        }
    }

    final override fun onOutputBufferAvailable(codec: MediaCodec, index: Int, info: MediaCodec.BufferInfo) {
        if (codecHolder.currentState.validOutputs) {
            onOutputBufferAvailable(codecHolder, index, info)
        }
    }

    final override fun onOutputFormatChanged(codec: MediaCodec, format: MediaFormat) {
        if (codecHolder.currentState.validOutputs) {
            onOutputFormatChanged(codecHolder, format)
        }
    }

    final override fun onError(codec: MediaCodec, exception: MediaCodec.CodecException) {
        onError(codecHolder, exception)
    }

    abstract fun onOutputBufferAvailable(codec: CodecHolder, index: Int, info: MediaCodec.BufferInfo)

    abstract fun onInputBufferAvailable(codec: CodecHolder, index: Int)

    abstract fun onOutputFormatChanged(codec: CodecHolder, format: MediaFormat)

    abstract fun onError(codec: CodecHolder, exception: MediaCodec.CodecException)
}
