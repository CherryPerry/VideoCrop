package ru.cherryperry.instavideo.domain.editor

import android.net.Uri
import io.reactivex.Single

/** Repository for retrieving media file's metadata. **/
interface MediaMetadataRepository {

    /** Returns video file information or exception [InvalidVideoFileException]. **/
    fun getMetaData(uri: Uri): Single<VideoFileMetaData>
}
