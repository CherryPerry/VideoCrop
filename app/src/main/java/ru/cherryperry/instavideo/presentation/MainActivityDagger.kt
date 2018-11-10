package ru.cherryperry.instavideo.presentation

import dagger.Module
import dagger.Subcomponent
import dagger.android.AndroidInjector

@Module(includes = [FragmentBuilder::class])
class MainActivityModule

@Subcomponent(modules = [MainActivityModule::class])
interface MainActivitySubcomponent : AndroidInjector<MainActivity> {

    @Subcomponent.Builder
    abstract class Builder : AndroidInjector.Builder<MainActivity>()
}
