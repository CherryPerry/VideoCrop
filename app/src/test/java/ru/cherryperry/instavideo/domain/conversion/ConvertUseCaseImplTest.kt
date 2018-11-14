package ru.cherryperry.instavideo.domain.conversion

import android.content.Context
import android.graphics.RectF
import android.net.Uri
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import io.mockk.Ordering
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RuntimeEnvironment
import ru.cherryperry.instavideo.data.media.conversion.VideoConverter
import java.io.File
import javax.inject.Provider

@RunWith(AndroidJUnit4::class)
class ConvertUseCaseImplTest {

    companion object {
        private val URI_SOURCE = Uri.parse("test://source")
        private val URI_TARGET = Uri.parse("test://target")
        private const val START_US = 10L
        private const val END_US = 20L
        private val CROP = RectF(0.3f, 0.4f, 0.5f, 0.6f)
    }

    private val converter = mockk<VideoConverter>(relaxUnitFun = true) {
        every { process(any(), any(), any(), any(), any(), any()) } answers {
            val callback: VideoConverter.Callback = it.invocation.args[5] as VideoConverter.Callback
            callback.onProgressChanged(0.5f)
        }
    }
    private val converterProvider: Provider<VideoConverter> = Provider { converter }
    private val fileProxy = mockk<FileProxy>(relaxUnitFun = true) {
        every { proxyFile } returns File(ApplicationProvider.getApplicationContext<Context>().filesDir, "test.file")
    }
    private val useCase = ConvertUseCaseImpl(converterProvider, fileProxy, RuntimeEnvironment.systemContext)

    @Test
    fun success() {
        useCase.run(ConvertParams(URI_SOURCE, URI_TARGET, START_US, END_US, CROP))
            .test()
            .await()
            .assertComplete()
            .assertValues(0f, 0.5f, 1f)
            .dispose()
        val path = fileProxy.proxyFile.absolutePath
        verify(ordering = Ordering.ORDERED) {
            converter.process(any(), START_US, END_US, CROP, path, any())
            fileProxy.copyProxyToResult(URI_TARGET)
            converter.close()
        }
    }

    @Test
    fun failureConverter() {
        every { converter.process(any(), any(), any(), any(), any(), any()) } throws RuntimeException()
        useCase.run(ConvertParams(URI_SOURCE, URI_TARGET, START_US, END_US, CROP))
            .test()
            .await()
            .assertError(RuntimeException::class.java)
            .dispose()
    }

    @Test
    fun failureFileProxy() {
        every { fileProxy.copyProxyToResult(any()) } throws RuntimeException()
        useCase.run(ConvertParams(URI_SOURCE, URI_TARGET, START_US, END_US, CROP))
            .test()
            .await()
            .assertError(RuntimeException::class.java)
            .dispose()
    }
}
