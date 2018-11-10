package ru.cherryperry.instavideo.presentation

import dagger.Binds
import dagger.Module
import dagger.android.AndroidInjector
import dagger.multibindings.ClassKey
import dagger.multibindings.IntoMap
import ru.cherryperry.instavideo.presentation.conversion.ConversionFragment
import ru.cherryperry.instavideo.presentation.conversion.ConversionFragmentSubcomponent
import ru.cherryperry.instavideo.presentation.editor.EditorFragment
import ru.cherryperry.instavideo.presentation.editor.EditorFragmentSubcomponent
import ru.cherryperry.instavideo.presentation.picker.PickerFragment
import ru.cherryperry.instavideo.presentation.picker.PickerFragmentSubcomponent

@Module(subcomponents = [
    PickerFragmentSubcomponent::class,
    EditorFragmentSubcomponent::class,
    ConversionFragmentSubcomponent::class
])
abstract class FragmentBuilder {

    @Binds
    @IntoMap
    @ClassKey(PickerFragment::class)
    abstract fun bindPickerFragment(builder: PickerFragmentSubcomponent.Builder): AndroidInjector.Factory<*>

    @Binds
    @IntoMap
    @ClassKey(EditorFragment::class)
    abstract fun bindEditorFragment(builder: EditorFragmentSubcomponent.Builder): AndroidInjector.Factory<*>

    @Binds
    @IntoMap
    @ClassKey(ConversionFragment::class)
    abstract fun bindConversionFragment(builder: ConversionFragmentSubcomponent.Builder): AndroidInjector.Factory<*>
}
