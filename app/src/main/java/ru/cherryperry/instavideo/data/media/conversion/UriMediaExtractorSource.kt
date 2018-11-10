package ru.cherryperry.instavideo.data.media.conversion

import android.content.Context
import android.media.MediaExtractor
import android.net.Uri

class UriMediaExtractorSource(
    private val uri: Uri,
    private val context: Context
) : MediaExtractorSource {

    override fun setDataSource(mediaExtractor: MediaExtractor) {
        mediaExtractor.setDataSource(context, uri, null)
    }
}
