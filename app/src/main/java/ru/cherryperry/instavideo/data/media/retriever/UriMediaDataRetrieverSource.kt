package ru.cherryperry.instavideo.data.media.retriever

import android.content.Context
import android.media.MediaMetadataRetriever
import android.net.Uri

class UriMediaDataRetrieverSource(
    private val uri: Uri,
    private val context: Context
) : MediaMetadataRetrieverSource {

    override fun setDataSource(mediaMetadataRetriever: MediaMetadataRetriever) {
        mediaMetadataRetriever.setDataSource(context, uri)
    }
}
