package ru.cherryperry.instavideo.presentation.conversion

import com.arellomobile.mvp.MvpView
import com.arellomobile.mvp.viewstate.strategy.AddToEndSingleStrategy
import com.arellomobile.mvp.viewstate.strategy.StateStrategyType

interface ConversionView : MvpView {

    @StateStrategyType(AddToEndSingleStrategy::class)
    fun showState(conversionScreenState: ConversionScreenState)
}
