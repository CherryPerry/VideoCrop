package ru.cherryperry.instavideo.data.media.retriever

import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Test
import org.junit.runner.RunWith
import ru.cherryperry.instavideo.AssetsInfo
import ru.cherryperry.instavideo.domain.editor.InvalidVideoFileException
import ru.cherryperry.instavideo.domain.editor.VideoFileMetaData
import ru.cherryperry.instavideo.testResources

@RunWith(AndroidJUnit4::class)
class MediaMetadataRepositoryImplTest {

    private val mediaMetadataRepositoryImpl = MediaMetadataRepositoryImpl(ApplicationProvider.getApplicationContext())

    @Test
    fun testGetMetaDataValidFile() {
        mediaMetadataRepositoryImpl
            .getMetaData(AssetMediaDataRetrieverSource(
                "sample_1.mp4",
                testResources().assets,
                ApplicationProvider.getApplicationContext()))
            .test()
            .await()
            .assertValue(VideoFileMetaData(1280, 720, AssetsInfo.SAMPLE_1_DURATION_MS))
            .dispose()
    }

    @Test
    fun testGetMetaDataRotatedFile() {
        mediaMetadataRepositoryImpl
            .getMetaData(AssetMediaDataRetrieverSource(
                "sample_1_rotated.mp4",
                testResources().assets,
                ApplicationProvider.getApplicationContext()))
            .test()
            .await()
            .assertValue(VideoFileMetaData(720, 1280, AssetsInfo.SAMPLE_1_DURATION_MS))
            .dispose()
    }

    @Test
    fun testGetMetaDataInvalidFile() {
        mediaMetadataRepositoryImpl
            .getMetaData(AssetMediaDataRetrieverSource(
                "sample_empty_file.mp4",
                testResources().assets,
                ApplicationProvider.getApplicationContext()))
            .test()
            .await()
            .assertError(InvalidVideoFileException::class.java)
            .dispose()
    }
}
