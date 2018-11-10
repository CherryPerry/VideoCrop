package ru.cherryperry.instavideo.data.media.retriever

import android.content.Context
import android.media.MediaMetadataRetriever
import android.net.Uri
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import ru.cherryperry.instavideo.domain.editor.InvalidVideoFileException
import ru.cherryperry.instavideo.domain.editor.MediaMetadataRepository
import ru.cherryperry.instavideo.domain.editor.VideoFileMetaData
import javax.inject.Inject

class MediaMetadataRepositoryImpl @Inject constructor(
    private val context: Context
) : MediaMetadataRepository {

    override fun getMetaData(uri: Uri): Single<VideoFileMetaData> = getMetaData(UriMediaDataRetrieverSource(uri, context))

    fun getMetaData(mediaMetadataRetrieverSource: MediaMetadataRetrieverSource): Single<VideoFileMetaData> = Single
        .fromCallable {
            val retriever = MediaMetadataRetriever()
            try {
                mediaMetadataRetrieverSource.setDataSource(retriever)
                val duration = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
                val width: String
                val height: String
                // video can be rotated
                val rotation = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_ROTATION)
                if (rotation == null || rotation == "0" || rotation == "180") {
                    width = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH)
                    height = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT)
                } else {
                    // if it is rotated to it's side, swap width and height from metadata
                    width = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT)
                    height = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH)
                }
                VideoFileMetaData(width.toLong(), height.toLong(), duration.toLong())
            } catch (exception: Exception) {
                throw InvalidVideoFileException()
            } finally {
                retriever.release()
            }
        }.subscribeOn(Schedulers.io())
}
