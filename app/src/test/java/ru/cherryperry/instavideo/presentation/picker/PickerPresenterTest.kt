package ru.cherryperry.instavideo.presentation.picker

import android.net.Uri
import androidx.test.ext.junit.runners.AndroidJUnit4
import io.mockk.mockk
import io.mockk.verify
import org.junit.Test
import org.junit.runner.RunWith
import ru.cherryperry.instavideo.presentation.navigation.EditorScreen
import ru.terrakok.cicerone.Router

@RunWith(AndroidJUnit4::class)
class PickerPresenterTest {

    @Test
    fun testOnVideoSelected() {
        val router = mockk<Router>(relaxUnitFun = true)
        val presenter = PickerPresenter(router)
        presenter.onVideoSelected(Uri.EMPTY)
        verify { router.navigateTo(EditorScreen(Uri.EMPTY)) }
    }
}
