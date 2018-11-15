package ru.cherryperry.instavideo.presentation.conversion

import android.graphics.RectF
import android.net.Uri
import androidx.test.ext.junit.runners.AndroidJUnit4
import io.mockk.every
import io.mockk.mockk
import io.mockk.verifyOrder
import io.reactivex.Flowable
import org.junit.Test
import org.junit.runner.RunWith
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

    private val useCase = mockk<ConvertUseCase> {
        every { run(ConvertParams(URI_SOURCE, URI_TARGET, START, END, RECT_F)) } returns Flowable.just(1f)
    }
    private val router = mockk<Router>(relaxUnitFun = true)
    private val view = mockk<ConversionView>(relaxUnitFun = true)
    private val presenter = ConversionPresenter(URI_SOURCE, URI_TARGET, START, END, RECT_F, useCase, router)

    @Test
    fun convertNormal() {
        presenter.attachView(view)
        verifyOrder {
            useCase.run(ConvertParams(URI_SOURCE, URI_TARGET, START, END, RECT_F))
            view.showProgress(1f)
            router.replaceScreen(CompleteScreen(URI_TARGET))
        }
    }

    @Test
    fun convertError() {
        every { useCase.run(ConvertParams(URI_SOURCE, URI_TARGET, START, END, RECT_F)) } returns
            Flowable.error(IllegalStateException())
        presenter.attachView(view)
        verifyOrder {
            useCase.run(ConvertParams(URI_SOURCE, URI_TARGET, START, END, RECT_F))
            router.replaceScreen(ErrorScreen)
        }
    }
}
