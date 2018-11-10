package ru.cherryperry.instavideo.presentation.picker

import android.net.Uri
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito
import ru.cherryperry.instavideo.presentation.navigation.EditorScreen
import ru.terrakok.cicerone.Router

@RunWith(AndroidJUnit4::class)
class PickerPresenterTest {

    @Test
    fun testOnVideoSelected() {
        val router = Mockito.mock(Router::class.java)
        val presenter = PickerPresenter(router)
        presenter.onVideoSelected(Uri.EMPTY)
        Mockito.verify(router).navigateTo(EditorScreen(Uri.EMPTY))
    }
}
