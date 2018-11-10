package ru.cherryperry.instavideo.presentation.editor

import dagger.Module
import dagger.Provides
import dagger.Subcomponent
import dagger.android.AndroidInjector

@Module
class EditorFragmentModule {

    @Provides
    fun videoUri(fragment: EditorFragment) = EditorFragment.videoUri(fragment.arguments!!)
}

@Subcomponent(modules = [EditorFragmentModule::class])
interface EditorFragmentSubcomponent : AndroidInjector<EditorFragment> {

    @Subcomponent.Builder
    abstract class Builder : AndroidInjector.Builder<EditorFragment>()
}
