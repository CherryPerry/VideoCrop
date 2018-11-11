package ru.cherryperry.instavideo.presentation.conversion

import dagger.Module
import dagger.Subcomponent
import dagger.android.AndroidInjector

@Module
class ErrorFragmentModule

@Subcomponent(modules = [ErrorFragmentModule::class])
interface ErrorFragmentSubcomponent : AndroidInjector<ErrorFragment> {

    @Subcomponent.Builder
    abstract class Builder : AndroidInjector.Builder<ErrorFragment>()
}
