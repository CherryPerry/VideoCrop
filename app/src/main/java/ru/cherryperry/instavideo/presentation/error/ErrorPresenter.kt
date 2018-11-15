package ru.cherryperry.instavideo.presentation.error

import com.arellomobile.mvp.InjectViewState
import ru.cherryperry.instavideo.presentation.base.BasePresenter
import javax.inject.Inject

@InjectViewState
open class ErrorPresenter @Inject constructor() : BasePresenter<ErrorView>()
