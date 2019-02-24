package ru.cherryperry.instavideo.data.media.conversion

import android.content.Context
import android.graphics.RectF
import android.media.MediaMetadataRetriever
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert
import org.junit.Rule
import org.junit.Test
import org.junit.rules.ExpectedException
import org.junit.runner.RunWith
import ru.cherryperry.instavideo.AssetsInfo
import ru.cherryperry.instavideo.BuildConfig
import ru.cherryperry.instavideo.data.media.retriever.use
import ru.cherryperry.instavideo.testResources
import java.io.File
import java.lang.Math.abs
import java.util.concurrent.TimeUnit

@RunWith(AndroidJUnit4::class)
class AsyncVideoConverterImplTest {

    companion object {
        private const val DURATION_DELTA = 300
    }

    @Rule
    @JvmField
    val expectedException: ExpectedException = ExpectedException.none()

    private val outputFile = File(ApplicationProvider.getApplicationContext<Context>().filesDir, "test.mp4").absolutePath

    @Test
    fun invalidStart() {
        expectedException.expect(IllegalArgumentException::class.java)
        AsyncVideoConverterImpl(ApplicationProvider.getApplicationContext()).use {
            it.process(EmptyMediaExtractorSource(), -1, 0, RectF(), outputFile,
                DefaultVideoConverterCallback())
        }
    }

    @Test
    fun invalidEnd() {
        expectedException.expect(IllegalArgumentException::class.java)
        AsyncVideoConverterImpl(ApplicationProvider.getApplicationContext()).use {
            it.process(EmptyMediaExtractorSource(), 0, -1, RectF(), outputFile,
                DefaultVideoConverterCallback())
        }
    }

    @Test
    fun invalidRange() {
        expectedException.expect(IllegalArgumentException::class.java)
        AsyncVideoConverterImpl(ApplicationProvider.getApplicationContext()).use {
            it.process(EmptyMediaExtractorSource(), 1, 0, RectF(), outputFile,
                DefaultVideoConverterCallback())
        }
    }

    @Test(timeout = BuildConfig.DEVICE_TEST_TIMEOUT)
    fun processSize1280x720FullLength() {
        AsyncVideoConverterImpl(ApplicationProvider.getApplicationContext()).use {
            it.process(
                AssetMediaExtractorSource("sample_1.mp4", testResources().assets),
                0,
                Long.MAX_VALUE,
                RectF(),
                outputFile,
                DefaultVideoConverterCallback()
            )
        }
        val file = File(outputFile)
        Assert.assertTrue(file.length() > 0)
        val duration = MediaMetadataRetriever().use {
            setDataSource(outputFile)
            extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION).toLong()
        }
        Assert.assertTrue(abs(AssetsInfo.SAMPLE_1_DURATION_MS - duration) < DURATION_DELTA)
    }

    @Test(timeout = BuildConfig.DEVICE_TEST_TIMEOUT)
    fun processSize1280x720Partial() {
        AsyncVideoConverterImpl(ApplicationProvider.getApplicationContext()).use {
            it.process(
                AssetMediaExtractorSource("sample_1.mp4", testResources().assets),
                TimeUnit.MILLISECONDS.toMicros(AssetsInfo.SAMPLE_1_DURATION_MS * 2 / 128),
                TimeUnit.MILLISECONDS.toMicros(AssetsInfo.SAMPLE_1_DURATION_MS * 3 / 128),
                RectF(),
                outputFile,
                DefaultVideoConverterCallback()
            )
        }
        val file = File(outputFile)
        Assert.assertTrue(file.length() > 0)
        val duration = MediaMetadataRetriever().use {
            setDataSource(outputFile)
            extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION).toLong()
        }
        Assert.assertTrue(abs(AssetsInfo.SAMPLE_1_DURATION_MS / 64 - duration) < DURATION_DELTA)
    }
}
