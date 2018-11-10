package ru.cherryperry.instavideo.data.media.conversion

import org.junit.Assert
import org.junit.Test

class ReportingFrameCallbackTest {

    @Test
    fun reportingFrameCallbackOneTrack() {
        val videoConverterCallback = CollectingVideoConverterCallback()
        val callback = VideoConverterImpl.ReportingFrameCallback(videoConverterCallback, 100, 1)
        callback.onFrameCompleted(1)
        callback.onFrameCompleted(10)
        callback.onFrameCompleted(100)
        Assert.assertEquals(listOf(0.01f, 0.1f, 1f), videoConverterCallback.results)
    }

    @Test
    fun reportingFrameCallbackTwoTracks() {
        val videoConverterCallback = CollectingVideoConverterCallback()
        val callback = VideoConverterImpl.ReportingFrameCallback(videoConverterCallback, 100, 2)
        callback.onFrameCompleted(1)
        callback.onFrameCompleted(10)
        callback.onFrameCompleted(100)
        callback.switchTrack()
        callback.onFrameCompleted(1)
        callback.onFrameCompleted(10)
        callback.onFrameCompleted(100)
        Assert.assertEquals(listOf(0.005f, 0.05f, 0.5f, 0.505f, 0.55f, 1f), videoConverterCallback.results)
    }

    @Test(expected = IllegalStateException::class)
    fun reportingFrameCallbackInvalidSwitchTrack() {
        val videoConverterCallback = DefaultVideoConverterCallback()
        val callback = VideoConverterImpl.ReportingFrameCallback(videoConverterCallback, 1, 1)
        callback.switchTrack()
    }

    private class CollectingVideoConverterCallback : VideoConverter.Callback {

        val results = mutableListOf<Float>()

        override fun onProgressChanged(progress: Float) {
            results += progress
        }
    }
}
