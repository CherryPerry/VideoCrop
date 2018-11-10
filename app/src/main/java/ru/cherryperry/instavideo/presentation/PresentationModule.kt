package ru.cherryperry.instavideo.presentation

import dagger.Binds
import dagger.Module
import ru.cherryperry.instavideo.presentation.util.saf.StorageAccessFramework
import ru.cherryperry.instavideo.presentation.util.saf.StorageAccessFrameworkImpl

@Module
abstract class PresentationModule {

    @Binds
    abstract fun storageAccessFramework(impl: StorageAccessFrameworkImpl): StorageAccessFramework
}
