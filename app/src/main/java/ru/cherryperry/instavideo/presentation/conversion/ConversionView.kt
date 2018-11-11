package ru.cherryperry.instavideo.presentation.conversion

import androidx.annotation.FloatRange
import com.arellomobile.mvp.MvpView
import com.arellomobile.mvp.viewstate.strategy.AddToEndSingleStrategy
import com.arellomobile.mvp.viewstate.strategy.StateStrategyType

interface ConversionView : MvpView {

    @StateStrategyType(AddToEndSingleStrategy::class)
    fun showProgress(@FloatRange(from = 0.0, to = 1.0) progress: Float)
}
