package ru.cherryperry.instavideo.presentation.conversion

import android.graphics.RectF
import android.net.Uri
import com.arellomobile.mvp.InjectViewState
import io.reactivex.android.schedulers.AndroidSchedulers
import ru.cherryperry.instavideo.domain.conversion.ConvertParams
import ru.cherryperry.instavideo.domain.conversion.ConvertUseCase
import ru.cherryperry.instavideo.presentation.base.BasePresenter
import ru.cherryperry.instavideo.presentation.navigation.CloseScreen
import ru.cherryperry.instavideo.presentation.navigation.OpenVideoScreen
import ru.cherryperry.instavideo.presentation.navigation.PickerScreen
import ru.terrakok.cicerone.Router
import javax.inject.Inject

@InjectViewState
open class ConversionPresenter @Inject constructor(
    @SourceUri private val sourceUri: Uri,
    @TargetUri private val targetUri: Uri,
    @StartTime private val startUs: Long,
    @EndTime private val endUs: Long,
    private val sourceRect: RectF,
    private val convertUseCase: ConvertUseCase,
    private val router: Router
) : BasePresenter<ConversionView>() {

    override fun onFirstViewAttach() {
        super.onFirstViewAttach()
        convertUseCase.run(ConvertParams(sourceUri, targetUri, startUs, endUs, sourceRect))
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                viewState.showState(ConversionScreenProgressState(it))
            }, {
                viewState.showState(ConversionScreenErrorState)
                // TODO Remove?
                it.printStackTrace()
            }, {
                viewState.showState(ConversionScreenCompleteState)
            })
            .untilDestroy()
    }

    fun onOpenResultClick() {
        router.navigateTo(OpenVideoScreen(targetUri))
    }

    fun onConvertAnotherClick() {
        router.replaceScreen(PickerScreen)
    }

    fun onCloseClick() {
        router.navigateTo(CloseScreen)
    }
}
