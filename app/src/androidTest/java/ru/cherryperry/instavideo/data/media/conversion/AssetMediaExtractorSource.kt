package ru.cherryperry.instavideo.data.media.conversion

import android.content.res.AssetManager
import android.media.MediaExtractor
import android.os.Build

class AssetMediaExtractorSource(
    private val assetName: String,
    private val assetsManager: AssetManager
) : MediaExtractorSource {

    override fun setDataSource(mediaExtractor: MediaExtractor) {
        assetsManager.openFd(assetName).use {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                mediaExtractor.setDataSource(it)
            } else {
                mediaExtractor.setDataSource(it.fileDescriptor, 0, it.length)
            }
        }
    }
}
