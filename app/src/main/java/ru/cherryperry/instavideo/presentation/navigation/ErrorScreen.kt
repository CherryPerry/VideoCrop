package ru.cherryperry.instavideo.presentation.navigation

import androidx.fragment.app.Fragment
import ru.cherryperry.instavideo.presentation.error.ErrorFragment
import ru.terrakok.cicerone.android.support.SupportAppScreen

object ErrorScreen : SupportAppScreen() {

    override fun getFragment(): Fragment = ErrorFragment.newInstance()
}
