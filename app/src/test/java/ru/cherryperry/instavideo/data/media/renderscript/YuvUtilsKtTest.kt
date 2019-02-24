package ru.cherryperry.instavideo.data.media.renderscript

import org.junit.Assert
import org.junit.Rule
import org.junit.Test
import org.junit.rules.ExpectedException

class YuvUtilsKtTest {

    @Rule
    @JvmField
    val expectedException: ExpectedException = ExpectedException.none()

    @Test
    fun yuvAllocationSizeNormal() {
        Assert.assertEquals(1382400, yuvAllocationSize(1280, 720))
    }

    @Test
    fun yuvAllocationSizeNegativeWidth() {
        expectedException.expect(IllegalArgumentException::class.java)
        yuvAllocationSize(-1, 20)
    }

    @Test
    fun yuvAllocationSizeNegativeHeight() {
        expectedException.expect(IllegalArgumentException::class.java)
        yuvAllocationSize(10, -1)
    }

    @Test
    fun yuvAllocationSizeZeroWidth() {
        expectedException.expect(IllegalArgumentException::class.java)
        yuvAllocationSize(0, 20)
    }

    @Test
    fun yuvAllocationSizeZeroHeight() {
        expectedException.expect(IllegalArgumentException::class.java)
        yuvAllocationSize(10, 0)
    }
}