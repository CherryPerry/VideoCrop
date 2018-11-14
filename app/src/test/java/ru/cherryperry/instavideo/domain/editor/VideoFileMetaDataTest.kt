package ru.cherryperry.instavideo.domain.editor

import org.junit.Assert
import org.junit.Rule
import org.junit.Test
import org.junit.rules.ExpectedException

class VideoFileMetaDataTest {

    @Rule
    @JvmField
    val expectedException: ExpectedException = ExpectedException.none()

    @Test
    fun valid() {
        val data = VideoFileMetaData(1, 2, 3)
        Assert.assertEquals(1, data.width)
        Assert.assertEquals(2, data.height)
        Assert.assertEquals(3, data.durationMs)
    }

    @Test
    fun invalidWidth() {
        expectedException.expect(IllegalArgumentException::class.java)
        VideoFileMetaData(0, 2, 3)
    }

    @Test
    fun invalidHeight() {
        expectedException.expect(IllegalArgumentException::class.java)
        VideoFileMetaData(1, 0, 3)
    }

    @Test
    fun invalidDuration() {
        expectedException.expect(IllegalArgumentException::class.java)
        VideoFileMetaData(1, 2, 0)
    }
}
