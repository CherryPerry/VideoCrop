package ru.cherryperry.instavideo.data.media.conversion

import android.media.MediaExtractor

class EmptyMediaExtractorSource : MediaExtractorSource {

    override fun setDataSource(mediaExtractor: MediaExtractor) {
        // nothing
    }
}
