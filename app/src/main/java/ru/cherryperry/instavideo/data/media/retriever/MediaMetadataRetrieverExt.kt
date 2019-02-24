package ru.cherryperry.instavideo.data.media.retriever

import android.media.MediaMetadataRetriever

fun <T> MediaMetadataRetriever.use(block: MediaMetadataRetriever.() -> T): T =
    try {
        block(this)
    } finally {
        this.release()
    }
