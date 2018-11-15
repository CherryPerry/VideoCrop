package ru.cherryperry.instavideo.presentation.complete

import android.net.Uri
import com.arellomobile.mvp.InjectViewState
import ru.cherryperry.instavideo.presentation.base.BasePresenter
import ru.cherryperry.instavideo.presentation.navigation.CloseScreen
import ru.cherryperry.instavideo.presentation.navigation.OpenVideoScreen
import ru.cherryperry.instavideo.presentation.navigation.PickerScreen
import ru.terrakok.cicerone.Router
import javax.inject.Inject

@InjectViewState
open class CompletePresenter @Inject constructor(
    private val targetUri: Uri,
    private val router: Router
) : BasePresenter<CompleteView>() {

    open fun onOpenResultClick() {
        router.navigateTo(OpenVideoScreen(targetUri))
    }

    open fun onConvertAnotherClick() {
        router.replaceScreen(PickerScreen)
    }

    open fun onCloseClick() {
        router.navigateTo(CloseScreen)
    }
}
