package ru.cherryperry.instavideo.presentation.conversion

import android.os.Bundle
import android.view.View
import com.arellomobile.mvp.presenter.InjectPresenter
import com.arellomobile.mvp.presenter.ProvidePresenter
import ru.cherryperry.instavideo.R
import ru.cherryperry.instavideo.presentation.base.BaseFragment
import javax.inject.Inject
import javax.inject.Provider

class ErrorFragment : BaseFragment(), ErrorView {

    companion object {

        fun newBundle() = Bundle()

        fun newInstance() =
            ErrorFragment().apply {
                arguments = newBundle()
            }
    }

    @Inject
    lateinit var presenterProvider: Provider<ErrorPresenter>
    @InjectPresenter
    lateinit var presenter: ErrorPresenter

    override val layoutId: Int = R.layout.error
    override val toolbarTitle: CharSequence?
        get() = getString(R.string.error_title)

    @ProvidePresenter
    fun providePresenter() = presenterProvider.get()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }
}
