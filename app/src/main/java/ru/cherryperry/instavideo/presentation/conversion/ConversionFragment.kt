package ru.cherryperry.instavideo.presentation.conversion

import android.graphics.RectF
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import com.arellomobile.mvp.presenter.InjectPresenter
import com.arellomobile.mvp.presenter.ProvidePresenter
import ru.cherryperry.instavideo.R
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

    private val loadingGroup by ViewDelegate<View>(R.id.loadingGroup, viewDelegateReset)
    private val progressView by ViewDelegate<ProgressBar>(R.id.progressBar, viewDelegateReset)
    private val errorGroup by ViewDelegate<View>(R.id.errorGroup, viewDelegateReset)
    private val completeGroup by ViewDelegate<View>(R.id.completeGroup, viewDelegateReset)
    private val openResultButton by ViewDelegate<View>(R.id.openResult, viewDelegateReset)
    private val convertAnotherButton by ViewDelegate<View>(R.id.convertAnother, viewDelegateReset)
    private val closeButton by ViewDelegate<View>(R.id.close, viewDelegateReset)

    override val layoutId: Int = R.layout.conversion
    override val toolbarTitle: CharSequence?
        get() = getString(R.string.conversion_title)

    @ProvidePresenter
    fun providePresenter() = presenterProvider.get()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        openResultButton.setOnClickListener { presenter.onOpenResultClick() }
        convertAnotherButton.setOnClickListener { presenter.onConvertAnotherClick() }
        closeButton.setOnClickListener { presenter.onCloseClick() }
    }

    override fun showState(conversionScreenState: ConversionScreenState) {
        when (conversionScreenState) {
            is ConversionScreenProgressState -> {
                loadingGroup.visibility = View.VISIBLE
                errorGroup.visibility = View.GONE
                completeGroup.visibility = View.GONE
                progressView.progress = (progressView.max * conversionScreenState.progress).roundToInt()
            }
            is ConversionScreenErrorState -> {
                errorGroup.visibility = View.VISIBLE
                loadingGroup.visibility = View.GONE
                completeGroup.visibility = View.GONE
            }
            is ConversionScreenCompleteState -> {
                completeGroup.visibility = View.VISIBLE
                loadingGroup.visibility = View.GONE
                errorGroup.visibility = View.GONE
            }
        }
    }
}
