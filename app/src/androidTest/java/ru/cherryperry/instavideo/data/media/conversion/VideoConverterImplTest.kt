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
import ru.cherryperry.instavideo.testResources
import java.io.File
import java.lang.Math.abs

@RunWith(AndroidJUnit4::class)
class VideoConverterImplTest {

    companion object {
        private const val DURATION_DELTA = 300
    }

    @Rule
    @JvmField
    val expectedException: ExpectedException = ExpectedException.none()

    private val outputFile = File(ApplicationProvider.getApplicationContext<Context>().filesDir, "test.mp4").absolutePath

    @Test
    fun yuvAllocationSizeNormal() {
        Assert.assertEquals(1382400, VideoConverterImpl.yuvAllocationSize(1280, 720))
    }

    @Test
    fun yuvAllocationSizeNegativeWidth() {
        expectedException.expect(IllegalArgumentException::class.java)
        VideoConverterImpl.yuvAllocationSize(-1, 20)
    }

    @Test
    fun yuvAllocationSizeNegativeHeight() {
        expectedException.expect(IllegalArgumentException::class.java)
        VideoConverterImpl.yuvAllocationSize(10, -1)
    }

    @Test
    fun yuvAllocationSizeZeroWidth() {
        expectedException.expect(IllegalArgumentException::class.java)
        VideoConverterImpl.yuvAllocationSize(0, 20)
    }

    @Test
    fun yuvAllocationSizeZeroHeight() {
        expectedException.expect(IllegalArgumentException::class.java)
        VideoConverterImpl.yuvAllocationSize(10, 0)
    }

    @Test
    fun invalidStart() {
        expectedException.expect(IllegalArgumentException::class.java)
        VideoConverterImpl(ApplicationProvider.getApplicationContext()).use {
            it.process(EmptyMediaExtractorSource(), -1, 0, RectF(), outputFile,
                DefaultVideoConverterCallback())
        }
    }

    @Test
    fun invalidEnd() {
        expectedException.expect(IllegalArgumentException::class.java)
        VideoConverterImpl(ApplicationProvider.getApplicationContext()).use {
            it.process(EmptyMediaExtractorSource(), 0, -1, RectF(), outputFile,
                DefaultVideoConverterCallback())
        }
    }

    @Test
    fun invalidRange() {
        expectedException.expect(IllegalArgumentException::class.java)
        VideoConverterImpl(ApplicationProvider.getApplicationContext()).use {
            it.process(EmptyMediaExtractorSource(), 1, 0, RectF(), outputFile,
                DefaultVideoConverterCallback())
        }
    }

    @Test
    fun processSize1280x720FullLength() {
        VideoConverterImpl(ApplicationProvider.getApplicationContext()).use {
            it.process(AssetMediaExtractorSource("sample_1.mp4", testResources().assets),
                0, Long.MAX_VALUE, RectF(), outputFile,
                DefaultVideoConverterCallback())
        }
        val file = File(outputFile)
        Assert.assertTrue(file.length() > 0)
        val duration = MediaMetadataRetriever().use {
            it.setDataSource(outputFile)
            it.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION).toLong()
        }
        Assert.assertTrue(abs(AssetsInfo.SAMPLE_1_DURATION_MS - duration) < DURATION_DELTA)
    }

    @Test
    fun processSize1280x720Partial() {
        VideoConverterImpl(ApplicationProvider.getApplicationContext()).use {
            it.process(AssetMediaExtractorSource("sample_1.mp4", testResources().assets),
                AssetsInfo.SAMPLE_1_DURATION_MS / 4 * 1000, AssetsInfo.SAMPLE_1_DURATION_MS * 3 / 4 * 1000,
                RectF(), outputFile, DefaultVideoConverterCallback())
        }
        val file = File(outputFile)
        Assert.assertTrue(file.length() > 0)
        val duration = MediaMetadataRetriever().use {
            it.setDataSource(outputFile)
            it.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION).toLong()
        }
        Assert.assertTrue(abs(AssetsInfo.SAMPLE_1_DURATION_MS / 2 - duration) < DURATION_DELTA)
    }

    private fun <T> MediaMetadataRetriever.use(block: (MediaMetadataRetriever) -> T): T {
        val retriever = MediaMetadataRetriever()
        try {
            return block(retriever)
        } finally {
            retriever.release()
        }
    }
}
