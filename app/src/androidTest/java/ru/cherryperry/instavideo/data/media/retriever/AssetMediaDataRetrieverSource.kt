package ru.cherryperry.instavideo.data.media.retriever

import android.content.Context
import android.content.res.AssetManager
import android.media.MediaMetadataRetriever
import android.net.Uri
import java.io.File
import java.io.FileOutputStream

class AssetMediaDataRetrieverSource(
    private val assetName: String,
    private val assetManager: AssetManager,
    private val targetContext: Context
) : MediaMetadataRetrieverSource {

    override fun setDataSource(mediaMetadataRetriever: MediaMetadataRetriever) {
        // Can't open asset by it's descriptor, just copy to file
        val file = File(targetContext.filesDir, assetName)
        assetManager.open(assetName).use { input ->
            FileOutputStream(file).use { output ->
                input.copyTo(output)
            }
        }
        mediaMetadataRetriever.setDataSource(targetContext, Uri.fromFile(file))
    }
}
