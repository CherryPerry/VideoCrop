package ru.cherryperry.instavideo.presentation.complete

import dagger.Module
import dagger.Provides
import dagger.Subcomponent
import dagger.android.AndroidInjector

@Module
class CompleteFragmentModule {

    @Provides
    fun targetUri(fragment: CompleteFragment) = CompleteFragment.targetUri(fragment.arguments!!)
}

@Subcomponent(modules = [CompleteFragmentModule::class])
interface CompleteFragmentSubcomponent : AndroidInjector<CompleteFragment> {

    @Subcomponent.Builder
    abstract class Builder : AndroidInjector.Builder<CompleteFragment>()
}
