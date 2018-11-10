package ru.cherryperry.instavideo.presentation.base

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import dagger.android.AndroidInjector
import dagger.android.DispatchingAndroidInjector
import dagger.android.support.AndroidSupportInjection
import dagger.android.support.HasSupportFragmentInjector
import ru.cherryperry.instavideo.R
import ru.cherryperry.instavideo.presentation.util.ViewDelegate
import ru.cherryperry.instavideo.presentation.util.ViewDelegateReset
import javax.inject.Inject

/**
 * MvpAppCompatFragment + DaggerFragment.
 */
abstract class BaseFragment : MvpAppCompatFragment(), HasSupportFragmentInjector {

    @Inject
    lateinit var childFragmentInjector: DispatchingAndroidInjector<Fragment>

    @get:LayoutRes
    protected open val layoutId: Int
        get() {
            throw NotImplementedError("Override this or onCreateView")
        }
    protected open val toolbarTitle: CharSequence?
        get() = getString(R.string.app_name)

    protected val viewDelegateReset = ViewDelegateReset()
    protected val toolbar by ViewDelegate<Toolbar?>(R.id.toolbar, viewDelegateReset)

    override fun onAttach(context: Context?) {
        AndroidSupportInjection.inject(this)
        super.onAttach(context)
    }

    override fun supportFragmentInjector(): AndroidInjector<Fragment> = childFragmentInjector

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
        inflater.inflate(layoutId, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        toolbar?.apply {
            title = toolbarTitle
            setNavigationOnClickListener { activity?.onBackPressed() }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        viewDelegateReset.onDestroyView()
    }
}
