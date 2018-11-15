package ru.cherryperry.instavideo.presentation.editor

import android.graphics.RectF
import android.net.Uri
import androidx.test.ext.junit.runners.AndroidJUnit4
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import io.mockk.verifyOrder
import io.reactivex.Single
import org.junit.Rule
import org.junit.Test
import org.junit.rules.ExpectedException
import org.junit.runner.RunWith
import ru.cherryperry.instavideo.domain.editor.VideoFileMetaData
import ru.cherryperry.instavideo.domain.editor.VideoFileMetaDataUseCase
import ru.cherryperry.instavideo.presentation.navigation.ConversionScreen
import ru.terrakok.cicerone.Router

@RunWith(AndroidJUnit4::class)
class EditorPresenterTest {

    companion object {
        private val URI_SOURCE = Uri.parse("test://source")
        private val URI_TARGET = Uri.parse("test://target")
        private val FILE_DATA = VideoFileMetaData(200, 100, 4)
    }

    @Rule
    @JvmField
    val expectedException: ExpectedException = ExpectedException.none()

    private val useCase = mockk<VideoFileMetaDataUseCase> {
        every { run(URI_SOURCE) } returns Single.just(FILE_DATA)
    }
    private val router = mockk<Router>(relaxUnitFun = true)
    private val view = mockk<EditorView>(relaxUnitFun = true)
    private val presenter = EditorPresenter(URI_SOURCE, useCase, router)

    @Test
    fun initialization() {
        // presenter should request video duration on first view attach
        presenter.attachView(view)
        verifyOrder {
            useCase.run(URI_SOURCE)
            view.showState(EditorView.State.LOADING)
            view.showState(EditorView.State.NORMAL)
            view.setVideoRatio(FILE_DATA.width, FILE_DATA.height)
        }
    }

    @Test
    fun initializationError() {
        // presenter should request video duration on first view attach
        every { useCase.run(URI_SOURCE) } returns Single.error(RuntimeException())
        presenter.attachView(view)
        verify { view.showState(EditorView.State.ERROR) }
    }

    @Test
    fun onSelectionChanged() {
        presenter.attachView(view)
        presenter.onSelectionChanged(0.25f, 0.75f)
        verify { view.showVideo(URI_SOURCE, 1000, 3000) }
    }

    @Test
    fun onSelectionChangedInvalidState() {
        expectedException.expect(IllegalStateException::class.java)
        presenter.onSelectionChanged(0f, 1f)
    }

    @Test
    fun onSelectionChangedInvalidArgsStartMoreThanEnd() {
        expectedException.expect(IllegalArgumentException::class.java)
        presenter.onSelectionChanged(1f, 0f)
    }

    @Test
    fun onSelectionChangedInvalidArgsStartLessThan0() {
        expectedException.expect(IllegalArgumentException::class.java)
        presenter.onSelectionChanged(-1f, 1f)
    }

    @Test
    fun onSelectionChangedInvalidArgsStartMoreThan1() {
        expectedException.expect(IllegalArgumentException::class.java)
        presenter.onSelectionChanged(2f, 1f)
    }

    @Test
    fun onSelectionChangedInvalidArgsEndLessThan0() {
        expectedException.expect(IllegalArgumentException::class.java)
        presenter.onSelectionChanged(0f, -1f)
    }

    @Test
    fun onSelectionChangedInvalidArgsEndMoreThan1() {
        expectedException.expect(IllegalArgumentException::class.java)
        presenter.onSelectionChanged(0f, 2f)
    }

    @Test
    fun onOutputSelected() {
        presenter.attachView(view)
        presenter.onSelectionChanged(0.25f, 0.75f)
        presenter.onOutputSelected(URI_TARGET, RectF())
        verify { router.replaceScreen(ConversionScreen(URI_SOURCE, URI_TARGET, 1000, 3000, RectF())) }
    }

    @Test
    fun onOutputSelectedInvalidState() {
        expectedException.expect(IllegalStateException::class.java)
        presenter.onOutputSelected(URI_TARGET, RectF())
    }
}
