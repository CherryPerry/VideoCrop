package ru.cherryperry.instavideo.domain.conversion

import android.net.Uri
import java.io.File

interface FileProxy {

    val proxyFile: File

    fun copyProxyToResult(uri: Uri)
}
