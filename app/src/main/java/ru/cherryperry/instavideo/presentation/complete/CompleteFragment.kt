package ru.cherryperry.instavideo.presentation.complete

import android.net.Uri
import android.os.Bundle
import android.view.View
import com.arellomobile.mvp.presenter.InjectPresenter
import com.arellomobile.mvp.presenter.ProvidePresenter
import dagger.Lazy
import ru.cherryperry.instavideo.R
import ru.cherryperry.instavideo.presentation.base.BaseFragment
import ru.cherryperry.instavideo.presentation.util.ViewDelegate
import javax.inject.Inject

class CompleteFragment : BaseFragment(), CompleteView {

    companion object {

        private const val TARGET_URI = "TargetUri"

        fun newBundle(targetUri: Uri) =
            Bundle().apply {
                putParcelable(TARGET_URI, targetUri)
            }

        fun newInstance(targetUri: Uri) =
            CompleteFragment().apply {
                arguments = newBundle(targetUri)
            }

        fun targetUri(bundle: Bundle): Uri = bundle.getParcelable(TARGET_URI)!!
    }

    @Inject
    lateinit var presenterProvider: Lazy<CompletePresenter>
    @InjectPresenter
    lateinit var presenter: CompletePresenter

    private val openResultButton by ViewDelegate<View>(R.id.openResult, viewDelegateReset)
    private val convertAnotherButton by ViewDelegate<View>(R.id.convertAnother, viewDelegateReset)
    private val closeButton by ViewDelegate<View>(R.id.close, viewDelegateReset)

    override val layoutId: Int = R.layout.complete
    override val toolbarTitle: CharSequence?
        get() = getString(R.string.complete_title)

    @ProvidePresenter
    fun providePresenter() = presenterProvider.get()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        openResultButton.setOnClickListener { presenter.onOpenResultClick() }
        convertAnotherButton.setOnClickListener { presenter.onConvertAnotherClick() }
        closeButton.setOnClickListener { presenter.onCloseClick() }
    }
}
