package ru.cherryperry.instavideo.presentation.conversion

import android.graphics.RectF
import android.net.Uri
import android.os.Bundle
import android.widget.ProgressBar
import androidx.annotation.FloatRange
import com.arellomobile.mvp.presenter.InjectPresenter
import com.arellomobile.mvp.presenter.ProvidePresenter
import ru.cherryperry.instavideo.R
import ru.cherryperry.instavideo.core.illegalArgument
import ru.cherryperry.instavideo.presentation.base.BaseFragment
import ru.cherryperry.instavideo.presentation.util.ViewDelegate
import javax.inject.Inject
import javax.inject.Provider
import kotlin.math.roundToInt

/**
 * Fragment with conversion process.
 * Used as loader.
 */
class ConversionFragment : BaseFragment(), ConversionView {

    companion object {

        private const val SOURCE_URI = "SourceUri"
        private const val TARGET_URI = "TargetUri"
        private const val START_US = "StartUs"
        private const val END_US = "EndUs"
        private const val SOURCE_RECT = "SourceRect"

        fun newBundle(
            sourceUri: Uri,
            targetUri: Uri,
            startUs: Long,
            endUs: Long,
            sourceRect: RectF
        ) = Bundle().apply {
            putParcelable(SOURCE_URI, sourceUri)
            putParcelable(TARGET_URI, targetUri)
            putLong(START_US, startUs)
            putLong(END_US, endUs)
            putParcelable(SOURCE_RECT, sourceRect)
        }

        fun newInstance(
            sourceUri: Uri,
            targetUri: Uri,
            start: Long,
            end: Long,
            sourceRect: RectF
        ): ConversionFragment = ConversionFragment().apply {
            arguments = newBundle(sourceUri, targetUri, start, end, sourceRect)
        }

        fun sourceUri(bundle: Bundle): Uri = bundle.getParcelable(SOURCE_URI)!!

        fun targetUri(bundle: Bundle): Uri = bundle.getParcelable(TARGET_URI)!!

        fun start(bundle: Bundle): Long = bundle.getLong(START_US)

        fun end(bundle: Bundle): Long = bundle.getLong(END_US)

        fun sourceRect(bundle: Bundle): RectF = bundle.getParcelable(SOURCE_RECT)!!
    }

    @Inject
    lateinit var presenterProvider: Provider<ConversionPresenter>
    @InjectPresenter
    lateinit var presenter: ConversionPresenter

    private val progressView by ViewDelegate<ProgressBar>(R.id.progressBar, viewDelegateReset)

    override val layoutId: Int = R.layout.conversion
    override val toolbarTitle: CharSequence?
        get() = getString(R.string.conversion_title)

    @ProvidePresenter
    fun providePresenter() = presenterProvider.get()

    override fun showProgress(@FloatRange(from = 0.0, to = 1.0) progress: Float) {
        (progress < 0 || progress > 1) illegalArgument "Progress should be in [0,1] range"
        progressView.progress = (progressView.max * progress).roundToInt()
    }
}
