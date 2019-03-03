package ru.cherryperry.instavideo.presentation.navigation

import android.net.Uri
import androidx.fragment.app.Fragment
import ru.cherryperry.instavideo.presentation.editor.EditorFragment
import ru.terrakok.cicerone.android.support.SupportAppScreen

data class EditorScreen(
    private val uri: Uri
) : SupportAppScreen() {

    override fun getFragment(): Fragment = EditorFragment.newInstance(uri)
}
