package ru.cherryperry.instavideo.presentation.conversion

import android.graphics.RectF
import android.net.Uri
import androidx.test.ext.junit.runners.AndroidJUnit4
import io.reactivex.Flowable
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito
import ru.cherryperry.instavideo.domain.conversion.ConvertParams
import ru.cherryperry.instavideo.domain.conversion.ConvertUseCase
import ru.cherryperry.instavideo.presentation.navigation.CompleteScreen
import ru.cherryperry.instavideo.presentation.navigation.ErrorScreen
import ru.terrakok.cicerone.Router

@RunWith(AndroidJUnit4::class)
class ConversionPresenterTest {

    companion object {
        private val URI_SOURCE = Uri.parse("test://source")
        private val URI_TARGET = Uri.parse("test://target")
        private const val START = 10L
        private const val END = 20L
        private val RECT_F = RectF(0.1f, 0.2f, 0.3f, 0.4f)
    }

    @Test
    fun convertNormal() {
        val useCase = Mockito.mock(ConvertUseCase::class.java)
        Mockito.`when`(useCase.run(ConvertParams(URI_SOURCE, URI_TARGET, START, END, RECT_F)))
            .thenReturn(Flowable.just(1f))
        val router = Mockito.mock(Router::class.java)
        val view = Mockito.mock(ConversionView::class.java)
        val presenter = ConversionPresenter(URI_SOURCE, URI_TARGET, START, END, RECT_F, useCase, router)
        presenter.attachView(view)
        Mockito.verify(useCase).run(ConvertParams(URI_SOURCE, URI_TARGET, START, END, RECT_F))
        Mockito.verify(view).showProgress(1f)
        Mockito.verify(router).replaceScreen(CompleteScreen(URI_TARGET))
    }

    @Test
    fun convertError() {
        val useCase = Mockito.mock(ConvertUseCase::class.java)
        Mockito.`when`(useCase.run(ConvertParams(URI_SOURCE, URI_TARGET, START, END, RECT_F)))
            .thenReturn(Flowable.error(IllegalStateException()))
        val router = Mockito.mock(Router::class.java)
        val view = Mockito.mock(ConversionView::class.java)
        val presenter = ConversionPresenter(URI_SOURCE, URI_TARGET, START, END, RECT_F, useCase, router)
        presenter.attachView(view)
        Mockito.verify(useCase).run(ConvertParams(URI_SOURCE, URI_TARGET, START, END, RECT_F))
        Mockito.verify(router).replaceScreen(ErrorScreen)
    }
}
