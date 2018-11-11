package ru.cherryperry.instavideo.presentation.navigation

import android.net.Uri
import androidx.fragment.app.Fragment
import ru.cherryperry.instavideo.presentation.conversion.CompleteFragment
import ru.cherryperry.instavideo.presentation.navigation.cicerone.SupportAppScreen

data class CompleteScreen(
    private val targetUri: Uri
) : SupportAppScreen() {

    override fun getFragment(): Fragment = CompleteFragment.newInstance(targetUri)
}
