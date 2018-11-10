package ru.cherryperry.instavideo.presentation.picker

import dagger.Subcomponent
import dagger.android.AndroidInjector

@Subcomponent
interface PickerFragmentSubcomponent : AndroidInjector<PickerFragment> {

    @Subcomponent.Builder
    abstract class Builder : AndroidInjector.Builder<PickerFragment>()
}
