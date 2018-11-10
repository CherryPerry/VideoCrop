package ru.cherryperry.instavideo.domain.editor

import android.net.Uri
import io.reactivex.Single
import javax.inject.Inject

class VideoFileMetaDataUseCaseImpl @Inject constructor(
    private val metadataRepository: MediaMetadataRepository
) : VideoFileMetaDataUseCase {

    override fun run(param: Uri): Single<VideoFileMetaData> = metadataRepository.getMetaData(param)
}
