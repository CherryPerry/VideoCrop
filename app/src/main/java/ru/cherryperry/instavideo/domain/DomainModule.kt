package ru.cherryperry.instavideo.domain

import dagger.Binds
import dagger.Module
import ru.cherryperry.instavideo.domain.conversion.ConvertUseCase
import ru.cherryperry.instavideo.domain.conversion.ConvertUseCaseImpl
import ru.cherryperry.instavideo.domain.editor.VideoFileMetaDataUseCase
import ru.cherryperry.instavideo.domain.editor.VideoFileMetaDataUseCaseImpl

@Module
abstract class DomainModule {

    @Binds
    abstract fun videoFileDurationUseCase(impl: VideoFileMetaDataUseCaseImpl): VideoFileMetaDataUseCase

    @Binds
    abstract fun convertUseCase(impl: ConvertUseCaseImpl): ConvertUseCase
}
