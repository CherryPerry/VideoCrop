package ru.cherryperry.instavideo.data.media.conversion

import android.media.MediaExtractor

interface MediaExtractorSource {

    fun setDataSource(mediaExtractor: MediaExtractor)
}
