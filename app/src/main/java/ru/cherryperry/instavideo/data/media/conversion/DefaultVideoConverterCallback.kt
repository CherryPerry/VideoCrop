package ru.cherryperry.instavideo.data.media.conversion

import androidx.annotation.FloatRange

/** Default implementation of [VideoConverter.Callback]. **/
class DefaultVideoConverterCallback : VideoConverter.Callback {

    override fun onProgressChanged(@FloatRange(from = 0.0, to = 1.0) progress: Float) {
        // nothing
    }
}
