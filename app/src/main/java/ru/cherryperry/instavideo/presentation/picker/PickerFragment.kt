package ru.cherryperry.instavideo.presentation.picker

import android.content.Intent
import android.os.Bundle
import android.view.View
import com.arellomobile.mvp.presenter.InjectPresenter
import com.arellomobile.mvp.presenter.ProvidePresenter
import ru.cherryperry.instavideo.R
import ru.cherryperry.instavideo.presentation.base.BaseFragment
import ru.cherryperry.instavideo.presentation.util.ViewDelegate
import ru.cherryperry.instavideo.presentation.util.saf.StorageAccessFramework
import javax.inject.Inject
import javax.inject.Provider

/**
 * Fragment with info and file selection button.
 */
class PickerFragment : BaseFragment(), PickerView {

    companion object {

        fun newInstance(): PickerFragment = PickerFragment()
    }

    @Inject
    lateinit var presenterProvider: Provider<PickerPresenter>
    @Inject
    lateinit var storageAccessFramework: StorageAccessFramework
    @InjectPresenter
    lateinit var presenter: PickerPresenter

    private val buttonView by ViewDelegate<View>(R.id.button, viewDelegateReset)

    override val layoutId = R.layout.picker
    override val toolbarTitle: CharSequence?
        get() = getString(R.string.picker_title)

    @ProvidePresenter
    fun providePresenter(): PickerPresenter = presenterProvider.get()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        toolbar?.navigationIcon = null
        buttonView.setOnClickListener {
            storageAccessFramework.open(this)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        storageAccessFramework.onActivityResultOpen(requestCode, resultCode, data)?.let {
            presenter.onVideoSelected(it)
        }
    }
}
