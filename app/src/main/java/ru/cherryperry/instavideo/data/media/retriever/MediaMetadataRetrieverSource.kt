package ru.cherryperry.instavideo.data.media.retriever

import android.media.MediaMetadataRetriever

interface MediaMetadataRetrieverSource {

    fun setDataSource(mediaMetadataRetriever: MediaMetadataRetriever)
}
