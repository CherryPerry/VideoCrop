package ru.cherryperry.instavideo.domain.editor

import org.junit.Assert
import org.junit.Test

class VideoFileMetaDataTest {

    @Test
    fun valid() {
        val data = VideoFileMetaData(1, 2, 3)
        Assert.assertEquals(1, data.width)
        Assert.assertEquals(2, data.height)
        Assert.assertEquals(3, data.durationMs)
    }

    @Test(expected = IllegalArgumentException::class)
    fun invalidWidth() {
        VideoFileMetaData(0, 2, 3)
    }

    @Test(expected = IllegalArgumentException::class)
    fun invalidHeight() {
        VideoFileMetaData(1, 0, 3)
    }

    @Test(expected = IllegalArgumentException::class)
    fun invalidDuration() {
        VideoFileMetaData(1, 2, 0)
    }
}
