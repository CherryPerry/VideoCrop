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
import ru.cherryperry.instavideo.presentation.navigation.CloseScreen
import ru.cherryperry.instavideo.presentation.navigation.OpenVideoScreen
import ru.cherryperry.instavideo.presentation.navigation.PickerScreen
import ru.terrakok.cicerone.Router

@RunWith(AndroidJUnit4::class)
class ConversionPresenterTest {

    companion object {
        private val URI_SOURCE = Uri.parse("test://source")
        private val URI_TARGET = Uri.parse("test://target")
    }

    @Test
    fun convertUseCaseNormal() {
        val useCase = Mockito.mock(ConvertUseCase::class.java)
        Mockito.`when`(useCase.run(ConvertParams(URI_SOURCE, URI_TARGET, 0, Long.MAX_VALUE, RectF())))
            .thenReturn(Flowable.just(1f))
        val router = Mockito.mock(Router::class.java)
        val view = Mockito.mock(ConversionView::class.java)
        val presenter = ConversionPresenter(URI_SOURCE, URI_TARGET, 0, Long.MAX_VALUE, RectF(), useCase, router)
        presenter.attachView(view)
        Mockito.verify(useCase).run(ConvertParams(URI_SOURCE, URI_TARGET, 0, Long.MAX_VALUE, RectF()))
        Mockito.verify(view).showState(ConversionScreenProgressState(1f))
        Mockito.verify(view).showState(ConversionScreenCompleteState)
    }

    @Test
    fun convertUseCaseError() {
        val useCase = Mockito.mock(ConvertUseCase::class.java)
        Mockito.`when`(useCase.run(ConvertParams(URI_SOURCE, URI_TARGET, 0, Long.MAX_VALUE, RectF())))
            .thenReturn(Flowable.error(IllegalStateException()))
        val router = Mockito.mock(Router::class.java)
        val view = Mockito.mock(ConversionView::class.java)
        val presenter = ConversionPresenter(URI_SOURCE, URI_TARGET, 0, Long.MAX_VALUE, RectF(), useCase, router)
        presenter.attachView(view)
        Mockito.verify(useCase).run(ConvertParams(URI_SOURCE, URI_TARGET, 0, Long.MAX_VALUE, RectF()))
        Mockito.verify(view).showState(ConversionScreenErrorState)
    }

    @Test
    fun onOpenResultClick() {
        val useCase = Mockito.mock(ConvertUseCase::class.java)
        Mockito.`when`(useCase.run(ConvertParams(URI_SOURCE, URI_TARGET, 0, Long.MAX_VALUE, RectF())))
            .thenReturn(Flowable.never())
        val router = Mockito.mock(Router::class.java)
        val view = Mockito.mock(ConversionView::class.java)
        val presenter = ConversionPresenter(URI_SOURCE, URI_TARGET, 0, Long.MAX_VALUE, RectF(), useCase, router)
        presenter.attachView(view)
        presenter.onOpenResultClick()
        Mockito.verify(router).navigateTo(OpenVideoScreen(URI_TARGET))
    }

    @Test
    fun onConvertAnotherClick() {
        val useCase = Mockito.mock(ConvertUseCase::class.java)
        Mockito.`when`(useCase.run(ConvertParams(URI_SOURCE, URI_TARGET, 0, Long.MAX_VALUE, RectF())))
            .thenReturn(Flowable.never())
        val router = Mockito.mock(Router::class.java)
        val view = Mockito.mock(ConversionView::class.java)
        val presenter = ConversionPresenter(URI_SOURCE, URI_TARGET, 0, Long.MAX_VALUE, RectF(), useCase, router)
        presenter.attachView(view)
        presenter.onConvertAnotherClick()
        Mockito.verify(router).replaceScreen(PickerScreen)
    }

    @Test
    fun onCloseClick() {
        val useCase = Mockito.mock(ConvertUseCase::class.java)
        Mockito.`when`(useCase.run(ConvertParams(URI_SOURCE, URI_TARGET, 0, Long.MAX_VALUE, RectF())))
            .thenReturn(Flowable.never())
        val router = Mockito.mock(Router::class.java)
        val view = Mockito.mock(ConversionView::class.java)
        val presenter = ConversionPresenter(URI_SOURCE, URI_TARGET, 0, Long.MAX_VALUE, RectF(), useCase, router)
        presenter.attachView(view)
        presenter.onCloseClick()
        Mockito.verify(router).navigateTo(CloseScreen)
    }
}
