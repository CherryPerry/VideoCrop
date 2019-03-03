package ru.cherryperry.instavideo.presentation.navigation

import androidx.fragment.app.Fragment
import ru.cherryperry.instavideo.presentation.picker.PickerFragment
import ru.terrakok.cicerone.android.support.SupportAppScreen

object PickerScreen : SupportAppScreen() {

    override fun getFragment(): Fragment = PickerFragment.newInstance()
}
