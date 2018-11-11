package ru.cherryperry.instavideo.presentation.complete

import android.net.Uri
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import org.junit.Test
import org.junit.runner.RunWith
import ru.cherryperry.instavideo.presentation.conversion.CompletePresenter
import ru.cherryperry.instavideo.presentation.navigation.CloseScreen
import ru.cherryperry.instavideo.presentation.navigation.OpenVideoScreen
import ru.cherryperry.instavideo.presentation.navigation.PickerScreen
import ru.terrakok.cicerone.Router

@RunWith(AndroidJUnit4::class)
class CompletePresenterTest {

    companion object {
        private val URI_TARGET = Uri.parse("test://target")
    }

    @Test
    fun onOpenResultClick() {
        val router = mock<Router>()
        val presenter = CompletePresenter(URI_TARGET, router)
        presenter.onOpenResultClick()
        verify(router).navigateTo(OpenVideoScreen(URI_TARGET))
    }

    @Test
    fun onConvertAnotherClick() {
        val router = mock<Router>()
        val presenter = CompletePresenter(URI_TARGET, router)
        presenter.onConvertAnotherClick()
        verify(router).navigateTo(PickerScreen)
    }

    @Test
    fun onCloseClick() {
        val router = mock<Router>()
        val presenter = CompletePresenter(URI_TARGET, router)
        presenter.onCloseClick()
        verify(router).navigateTo(CloseScreen)
    }
}
