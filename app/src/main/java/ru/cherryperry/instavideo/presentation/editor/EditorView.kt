package ru.cherryperry.instavideo.presentation.editor

import android.net.Uri
import com.arellomobile.mvp.MvpView
import com.arellomobile.mvp.viewstate.strategy.AddToEndSingleStrategy
import com.arellomobile.mvp.viewstate.strategy.StateStrategyType

interface EditorView : MvpView {

    @StateStrategyType(AddToEndSingleStrategy::class)
    fun setVideoRatio(width: Long, height: Long)

    @StateStrategyType(AddToEndSingleStrategy::class)
    fun showVideo(uri: Uri, fromUs: Long, toUs: Long)

    @StateStrategyType(AddToEndSingleStrategy::class)
    fun showState(state: State)

    @StateStrategyType(AddToEndSingleStrategy::class)
    fun limitSelection(range: Float)

    enum class State {
        LOADING, NORMAL, ERROR
    }
}
