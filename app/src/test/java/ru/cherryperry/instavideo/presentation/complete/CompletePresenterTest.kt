package ru.cherryperry.instavideo.presentation.complete

import android.net.Uri
import androidx.test.ext.junit.runners.AndroidJUnit4
import io.mockk.mockk
import io.mockk.verify
import org.junit.Test
import org.junit.runner.RunWith
import ru.cherryperry.instavideo.presentation.navigation.CloseScreen
import ru.cherryperry.instavideo.presentation.navigation.OpenVideoScreen
import ru.cherryperry.instavideo.presentation.navigation.PickerScreen
import ru.terrakok.cicerone.Router

@RunWith(AndroidJUnit4::class)
class CompletePresenterTest {

    companion object {
        private val URI_TARGET = Uri.parse("test://target")
    }

    private val router = mockk<Router>(relaxUnitFun = true)
    private val presenter = CompletePresenter(URI_TARGET, router)

    @Test
    fun onOpenResultClick() {
        presenter.onOpenResultClick()
        verify { router.navigateTo(OpenVideoScreen(URI_TARGET)) }
    }

    @Test
    fun onConvertAnotherClick() {
        presenter.onConvertAnotherClick()
        verify { router.replaceScreen(PickerScreen) }
    }

    @Test
    fun onCloseClick() {
        presenter.onCloseClick()
        verify { router.navigateTo(CloseScreen) }
    }
}
