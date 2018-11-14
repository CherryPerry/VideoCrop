package ru.cherryperry.instavideo.data.file

import android.content.Context
import android.net.Uri
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File

@RunWith(AndroidJUnit4::class)
class FileProxyImplTest {

    private val context = ApplicationProvider.getApplicationContext<Context>()
    private val fileProxyImpl = FileProxyImpl(context)

    @Test
    fun proxyFile() {
        val filesDir = context.filesDir
        Assert.assertTrue(fileProxyImpl.proxyFile.startsWith(filesDir))
    }

    @Test
    fun copyProxyToResult() {
        val text = "Text"
        fileProxyImpl.proxyFile.writeText(text)
        val targetFile = File(context.cacheDir, "target")
        fileProxyImpl.copyProxyToResult(Uri.fromFile(targetFile))
        Assert.assertEquals(text, targetFile.readText())
    }
}
