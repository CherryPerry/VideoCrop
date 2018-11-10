package ru.cherryperry.instavideo.presentation.navigation

import androidx.fragment.app.Fragment
import ru.cherryperry.instavideo.presentation.navigation.cicerone.SupportAppScreen
import ru.cherryperry.instavideo.presentation.picker.PickerFragment

object PickerScreen : SupportAppScreen() {

    override fun getFragment(): Fragment = PickerFragment.newInstance()
}
