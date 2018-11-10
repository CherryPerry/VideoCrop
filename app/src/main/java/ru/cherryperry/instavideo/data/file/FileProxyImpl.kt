package ru.cherryperry.instavideo.data.file

import android.content.Context
import android.net.Uri
import ru.cherryperry.instavideo.domain.conversion.FileProxy
import java.io.File
import javax.inject.Inject

class FileProxyImpl @Inject constructor(
    private val context: Context
) : FileProxy {

    companion object {
        private const val FILE_NAME = "result.mp4"
    }

    override val proxyFile: File = File(context.filesDir, FILE_NAME)

    override fun copyProxyToResult(uri: Uri) {
        context.contentResolver.openOutputStream(uri)?.use { output ->
            proxyFile.inputStream().use { input ->
                input.copyTo(output)
            }
        }
    }
}
