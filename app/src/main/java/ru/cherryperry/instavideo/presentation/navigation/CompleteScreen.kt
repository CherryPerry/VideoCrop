package ru.cherryperry.instavideo.presentation.navigation

import android.net.Uri
import androidx.fragment.app.Fragment
import ru.cherryperry.instavideo.presentation.complete.CompleteFragment
import ru.terrakok.cicerone.android.support.SupportAppScreen

data class CompleteScreen(
    private val targetUri: Uri
) : SupportAppScreen() {

    override fun getFragment(): Fragment = CompleteFragment.newInstance(targetUri)
}
