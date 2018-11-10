package ru.cherryperry.instavideo.data.media.codec

import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MediaCodecFactoryTest {

    @Test
    fun testCreateVideoEncoder() {
        // test it was created and configured successfully
        MediaCodecFactory.createVideoEncoder()
    }

    @Test
    fun testCreateAudioEncoder() {
        // test it was created and configured successfully
        MediaCodecFactory.createAudioEncoder()
    }
}
