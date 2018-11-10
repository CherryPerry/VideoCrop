package ru.cherryperry.instavideo.presentation.picker

import android.net.Uri
import com.arellomobile.mvp.InjectViewState
import ru.cherryperry.instavideo.presentation.base.BasePresenter
import ru.cherryperry.instavideo.presentation.navigation.EditorScreen
import ru.terrakok.cicerone.Router
import javax.inject.Inject

@InjectViewState
open class PickerPresenter @Inject constructor(
    private val router: Router
) : BasePresenter<PickerView>() {

    open fun onVideoSelected(uri: Uri) {
        router.navigateTo(EditorScreen(uri))
    }
}
