package ru.cherryperry.instavideo.presentation.conversion

import androidx.annotation.FloatRange

sealed class ConversionScreenState

object ConversionScreenErrorState : ConversionScreenState()

data class ConversionScreenProgressState(
    @FloatRange(from = 0.0, to = 1.0) val progress: Float
) : ConversionScreenState() {

    init {
        if (progress < 0 || progress > 1) {
            throw IllegalArgumentException("Progress should be in [0,1] range")
        }
    }
}

object ConversionScreenCompleteState : ConversionScreenState()
