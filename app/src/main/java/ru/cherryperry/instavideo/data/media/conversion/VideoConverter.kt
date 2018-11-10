package ru.cherryperry.instavideo.data.media.conversion

import android.graphics.RectF
import androidx.annotation.FloatRange
import java.io.Closeable

/**
 * Converter from one video file to another.
 * Should be provided as [javax.inject.Provider] in non-closable classes to close it correctly.
 */
interface VideoConverter : Closeable {

    /** Converts [MediaExtractorSource] and writes output to file by [outputFile] path. **/
    fun process(
        source: MediaExtractorSource,
        startUs: Long,
        endUs: Long,
        sourceRect: RectF,
        outputFile: String,
        callback: Callback
    )

    interface Callback {

        /** Conversion progress changed. **/
        fun onProgressChanged(@FloatRange(from = 0.0, to = 1.0) progress: Float)
    }
}
