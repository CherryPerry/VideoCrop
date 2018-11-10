package ru.cherryperry.instavideo.domain.editor

import android.net.Uri
import androidx.test.ext.junit.runners.AndroidJUnit4
import io.reactivex.Single
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito

@RunWith(AndroidJUnit4::class)
class VideoFileMetaDataUseCaseImplTest {

    private val mediaMetadataRepository = Mockito.mock(MediaMetadataRepository::class.java)
    private val useCase = VideoFileMetaDataUseCaseImpl(mediaMetadataRepository)

    @Test
    fun success() {
        val result = VideoFileMetaData(1, 1, 1)
        Mockito.`when`(mediaMetadataRepository.getMetaData(Uri.EMPTY))
            .thenReturn(Single.just(result))
        useCase.run(Uri.EMPTY)
            .test()
            .await()
            .assertValue(result)
            .dispose()
    }

    @Test
    fun failure() {
        val result = InvalidVideoFileException()
        Mockito.`when`(mediaMetadataRepository.getMetaData(Uri.EMPTY))
            .thenReturn(Single.error(result))
        useCase.run(Uri.EMPTY)
            .test()
            .await()
            .assertError(result)
            .dispose()
    }
}
