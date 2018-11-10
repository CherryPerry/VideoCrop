package ru.cherryperry.instavideo.data.media.extractor

import android.media.MediaExtractor

/**
 * Select track by index from [MediaExtractorData].
 */
fun MediaExtractor.selectTrack(mediaExtractorData: MediaExtractorData) {
    this.selectTrack(mediaExtractorData.index)
}

/**
 * Select track by index from [MediaExtractorData].
 */
fun MediaExtractor.unselectTrack(mediaExtractorData: MediaExtractorData) {
    this.unselectTrack(mediaExtractorData.index)
}

/**
 * Extract track's format to [MediaExtractorData] by it's [index].
 */
fun MediaExtractor.getTrackData(index: Int): MediaExtractorData =
    MediaExtractorData(index, this.getTrackFormat(index))
