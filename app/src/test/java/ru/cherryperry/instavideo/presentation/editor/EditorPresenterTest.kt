package ru.cherryperry.instavideo.presentation.editor

import android.graphics.RectF
import android.net.Uri
import androidx.test.ext.junit.runners.AndroidJUnit4
import io.reactivex.Single
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito
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

    @Test
    fun initialization() {
        // presenter should request video duration on first view attach
        val useCase = Mockito.mock(VideoFileMetaDataUseCase::class.java)
        Mockito.`when`(useCase.run(URI_SOURCE)).thenReturn(Single.just(FILE_DATA))
        val router = Mockito.mock(Router::class.java)
        val view = Mockito.mock(EditorView::class.java)
        val presenter = EditorPresenter(URI_SOURCE, useCase, router)
        presenter.attachView(view)
        Mockito.verify(useCase).run(URI_SOURCE)
        Mockito.verify(view).showState(EditorView.State.LOADING)
        Mockito.verify(view).showState(EditorView.State.NORMAL)
        Mockito.verify(view).setVideoRatio(FILE_DATA.width, FILE_DATA.height)
    }

    @Test
    fun initializationError() {
        // presenter should request video duration on first view attach
        val useCase = Mockito.mock(VideoFileMetaDataUseCase::class.java)
        Mockito.`when`(useCase.run(URI_SOURCE)).thenReturn(Single.error(IllegalStateException()))
        val router = Mockito.mock(Router::class.java)
        val view = Mockito.mock(EditorView::class.java)
        val presenter = EditorPresenter(URI_SOURCE, useCase, router)
        presenter.attachView(view)
        Mockito.verify(view).showState(EditorView.State.ERROR)
    }

    @Test
    fun onSelectionChanged() {
        val useCase = Mockito.mock(VideoFileMetaDataUseCase::class.java)
        Mockito.`when`(useCase.run(URI_SOURCE)).thenReturn(Single.just(FILE_DATA))
        val router = Mockito.mock(Router::class.java)
        val view = Mockito.mock(EditorView::class.java)
        val presenter = EditorPresenter(URI_SOURCE, useCase, router)
        presenter.attachView(view)
        presenter.onSelectionChanged(0.25f, 0.75f)
        Mockito.verify(view).showVideo(URI_SOURCE, 1000, 3000)
    }

    @Test(expected = IllegalStateException::class)
    fun onSelectionChangedInvalidState() {
        createDefaultPresenter().onSelectionChanged(0f, 1f)
    }

    @Test(expected = IllegalArgumentException::class)
    fun onSelectionChangedInvalidArgsStartMoreThanEnd() {
        createDefaultPresenter().onSelectionChanged(1f, 0f)
    }

    @Test(expected = IllegalArgumentException::class)
    fun onSelectionChangedInvalidArgsStartLessThan0() {
        createDefaultPresenter().onSelectionChanged(-1f, 1f)
    }

    @Test(expected = IllegalArgumentException::class)
    fun onSelectionChangedInvalidArgsStartMoreThan1() {
        createDefaultPresenter().onSelectionChanged(2f, 1f)
    }

    @Test(expected = IllegalArgumentException::class)
    fun onSelectionChangedInvalidArgsEndLessThan0() {
        createDefaultPresenter().onSelectionChanged(0f, -1f)
    }

    @Test(expected = IllegalArgumentException::class)
    fun onSelectionChangedInvalidArgsEndMoreThan1() {
        createDefaultPresenter().onSelectionChanged(0f, 2f)
    }

    @Test
    fun onOutputSelected() {
        val useCase = Mockito.mock(VideoFileMetaDataUseCase::class.java)
        Mockito.`when`(useCase.run(URI_SOURCE)).thenReturn(Single.just(FILE_DATA))
        val router = Mockito.mock(Router::class.java)
        val view = Mockito.mock(EditorView::class.java)
        val presenter = EditorPresenter(URI_SOURCE, useCase, router)
        presenter.attachView(view)
        presenter.onSelectionChanged(0.25f, 0.75f)
        presenter.onOutputSelected(URI_TARGET, RectF())
        Mockito.verify(router).replaceScreen(ConversionScreen(URI_SOURCE, URI_TARGET, 1000, 3000, RectF()))
    }

    @Test(expected = IllegalStateException::class)
    fun onOutputSelectedInvalidState() {
        createDefaultPresenter().onOutputSelected(URI_TARGET, RectF())
    }

    private fun createDefaultPresenter(): EditorPresenter {
        val useCase = Mockito.mock(VideoFileMetaDataUseCase::class.java)
        val router = Mockito.mock(Router::class.java)
        return EditorPresenter(URI_SOURCE, useCase, router)
    }
}
