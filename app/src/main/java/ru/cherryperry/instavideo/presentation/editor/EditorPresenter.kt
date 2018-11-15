package ru.cherryperry.instavideo.presentation.editor

import android.graphics.RectF
import android.net.Uri
import androidx.annotation.FloatRange
import com.arellomobile.mvp.InjectViewState
import com.google.android.exoplayer2.C
import io.reactivex.android.schedulers.AndroidSchedulers
import ru.cherryperry.instavideo.core.illegalArgument
import ru.cherryperry.instavideo.core.illegalState
import ru.cherryperry.instavideo.domain.editor.VideoFileMetaDataUseCase
import ru.cherryperry.instavideo.presentation.base.BasePresenter
import ru.cherryperry.instavideo.presentation.navigation.ConversionScreen
import ru.terrakok.cicerone.Router
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@InjectViewState
open class EditorPresenter @Inject constructor(
    private val uri: Uri,
    private val videoFileMetaDataUseCase: VideoFileMetaDataUseCase,
    private val router: Router
) : BasePresenter<EditorView>() {

    companion object {
        private val VIDEO_LIMIT_US = TimeUnit.SECONDS.toMicros(20)
    }

    private var durationUs: Long = C.TIME_UNSET
    private var startUs: Long = C.TIME_UNSET
    private var endUs: Long = C.TIME_UNSET

    override fun onFirstViewAttach() {
        super.onFirstViewAttach()
        videoFileMetaDataUseCase.run(uri)
            .observeOn(AndroidSchedulers.mainThread())
            .doOnSubscribe { viewState.showState(EditorView.State.LOADING) }
            .subscribe({
                durationUs = TimeUnit.MILLISECONDS.toMicros(it.durationMs)
                viewState.showState(EditorView.State.NORMAL)
                viewState.setVideoRatio(it.width, it.height)
                viewState.showVideo(uri, startUs, endUs)
                viewState.limitSelection(VIDEO_LIMIT_US / durationUs.toFloat())
            }, {
                viewState.showState(EditorView.State.ERROR)
            })
            .untilDestroy()
    }

    open fun onPlayVideoError() {
        viewState.showState(EditorView.State.ERROR)
    }

    /** User selects different time span of video. */
    open fun onSelectionChanged(
        @FloatRange(from = 0.0, to = 1.0) from: Float,
        @FloatRange(from = 0.0, to = 1.0) to: Float
    ) {
        (from < 0 || from > 1) illegalArgument "Must be in range [0,1]"
        (to < 0 || to > 1) illegalArgument "Must be in range [0,1]"
        (from > to) illegalArgument "Start can't be later than end"
        (durationUs == C.TIME_UNSET) illegalState "Duration is not ready yet"
        ((durationUs * (to - from)).toLong() > VIDEO_LIMIT_US) illegalArgument "Selection is too big"
        startUs = (durationUs * from).toLong()
        endUs = (durationUs * to).toLong()
        viewState.showVideo(uri, startUs, endUs)
    }

    /** User has selected conversion result file, start conversion now. */
    open fun onOutputSelected(targetUri: Uri, sourceRect: RectF) {
        (durationUs == C.TIME_UNSET) illegalState "Duration is not ready yet"
        val start = if (startUs == C.TIME_UNSET) 0 else startUs
        val end = if (endUs == C.TIME_UNSET) Long.MAX_VALUE else endUs
        router.replaceScreen(ConversionScreen(uri, targetUri, start, end, sourceRect))
    }
}
