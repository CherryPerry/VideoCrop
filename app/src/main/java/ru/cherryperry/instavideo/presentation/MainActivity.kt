package ru.cherryperry.instavideo.presentation

import android.os.Bundle
import ru.cherryperry.instavideo.presentation.base.BaseActivity
import ru.cherryperry.instavideo.presentation.navigation.CustomSupportAppNavigator
import ru.cherryperry.instavideo.presentation.navigation.PickerScreen
import ru.terrakok.cicerone.NavigatorHolder
import ru.terrakok.cicerone.Router
import javax.inject.Inject

class MainActivity : BaseActivity() {

    @Inject
    lateinit var navigatorHolder: NavigatorHolder
    @Inject
    lateinit var router: Router

    private lateinit var navigator: CustomSupportAppNavigator

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        navigator = CustomSupportAppNavigator(this, android.R.id.content)
        if (savedInstanceState == null) {
            router.replaceScreen(PickerScreen)
        }
    }

    override fun onResumeFragments() {
        super.onResumeFragments()
        navigatorHolder.setNavigator(navigator)
    }

    override fun onPause() {
        super.onPause()
        navigatorHolder.removeNavigator()
    }
}
