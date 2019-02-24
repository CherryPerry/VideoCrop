package ru.cherryperry.instavideo.data

import dagger.Binds
import dagger.Module
import ru.cherryperry.instavideo.data.file.FileProxyImpl
import ru.cherryperry.instavideo.data.media.conversion.AsyncVideoConverterImpl
import ru.cherryperry.instavideo.data.media.conversion.VideoConverter
import ru.cherryperry.instavideo.data.media.retriever.MediaMetadataRepositoryImpl
import ru.cherryperry.instavideo.domain.conversion.FileProxy
import ru.cherryperry.instavideo.domain.editor.MediaMetadataRepository

@Module
abstract class DataModule {

    @Binds
    abstract fun mediaMetadataRepository(impl: MediaMetadataRepositoryImpl): MediaMetadataRepository

    @Binds
    abstract fun videoConverter(impl: AsyncVideoConverterImpl): VideoConverter

    @Binds
    abstract fun fileProxy(impl: FileProxyImpl): FileProxy
}
