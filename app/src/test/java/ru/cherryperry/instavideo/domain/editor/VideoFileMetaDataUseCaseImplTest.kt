package ru.cherryperry.instavideo.domain.editor

import android.net.Uri
import androidx.test.ext.junit.runners.AndroidJUnit4
import io.mockk.every
import io.mockk.mockk
import io.reactivex.Single
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class VideoFileMetaDataUseCaseImplTest {

    private val mediaMetadataRepository = mockk<MediaMetadataRepository> {}
    private val useCase = VideoFileMetaDataUseCaseImpl(mediaMetadataRepository)

    @Test
    fun success() {
        val result = VideoFileMetaData(1, 1, 1)
        every { mediaMetadataRepository.getMetaData(Uri.EMPTY) } returns Single.just(result)
        useCase.run(Uri.EMPTY)
            .test()
            .await()
            .assertValue(result)
            .dispose()
    }

    @Test
    fun failure() {
        val throwable = InvalidVideoFileException(Exception())
        every { mediaMetadataRepository.getMetaData(Uri.EMPTY) } returns Single.error(throwable)
        useCase.run(Uri.EMPTY)
            .test()
            .await()
            .assertError(throwable)
            .dispose()
    }
}
