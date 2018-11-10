package ru.cherryperry.instavideo.data.media.extractor

import android.media.MediaFormat

/**
 * [MediaFormat] with it's [index] from [android.media.MediaExtractor].
 */
class MediaExtractorData(
    val index: Int,
    val mediaFormat: MediaFormat
) {

    val mimeType: String = mediaFormat.getString(MediaFormat.KEY_MIME)
}
