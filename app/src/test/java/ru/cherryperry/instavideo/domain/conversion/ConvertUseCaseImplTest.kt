package ru.cherryperry.instavideo.domain.conversion

import android.content.Context
import android.graphics.RectF
import android.net.Uri
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.doAnswer
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
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

    private val converter = mock<VideoConverter> {
        on { process(any(), any(), any(), any(), any(), any()) }.doAnswer {
            val callback: VideoConverter.Callback = it.getArgument(5)
            callback.onProgressChanged(0.5f)
            null
        }
    }
    private val converterProvider: Provider<VideoConverter> = Provider { converter }
    private val fileProxy = mock<FileProxy> {
        on { proxyFile }.thenReturn(File(ApplicationProvider.getApplicationContext<Context>().filesDir, "test.file"))
    }

    @Test
    fun valid() {
        val useCase = ConvertUseCaseImpl(converterProvider, fileProxy, RuntimeEnvironment.systemContext)
        useCase.run(ConvertParams(URI_SOURCE, URI_TARGET, START_US, END_US, CROP))
            .test()
            .await()
            .assertComplete()
            .assertValues(0f, 0.5f, 1f)
            .dispose()
        val converter = this.converter
        // TODO
        // verify(converter).process(any(), eq(START_US), eq(END_US), eq(CROP), eq(fileProxy.proxyFile.absolutePath), any())
        verify(converter).close()
        val fileProxy = this.fileProxy
        verify(fileProxy).copyProxyToResult(URI_TARGET)
    }
}
