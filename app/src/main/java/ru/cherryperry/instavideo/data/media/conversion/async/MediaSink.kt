package ru.cherryperry.instavideo.data.media.conversion.async

import android.media.MediaCodec
import android.media.MediaFormat
import android.media.MediaMuxer
import ru.cherryperry.instavideo.core.illegalState
import java.nio.ByteBuffer
import java.util.concurrent.CountDownLatch

class MediaSink(
    private val mediaMuxer: MediaMuxer,
    private val tracks: Int
) {

    private val map = mutableMapOf<String, Int>()
    private val countDownLatch = CountDownLatch(tracks)
    private val configured = false

    fun applyCodec(mediaFormat: MediaFormat): Boolean {
        if (configured) {
            return false
        }
        val mime = mediaFormat.getString(MediaFormat.KEY_MIME)
        if (map.containsKey(mime)) {
            return false
        }
        synchronized(mediaMuxer) {
            val index = mediaMuxer.addTrack(mediaFormat)
            map[mediaFormat.getString(MediaFormat.KEY_MIME)] = index
            if (map.size == tracks) {
                mediaMuxer.start()
            }
        }
        countDownLatch.countDown()
        return true
    }

    fun sink(mediaFormat: MediaFormat, byteBuffer: ByteBuffer, bufferInfo: MediaCodec.BufferInfo) {
        if (!configured) {
            countDownLatch.await()
        }
        val index: Int? = map[mediaFormat.getString(MediaFormat.KEY_MIME)]
        (index == null) illegalState "MediaTrack is not added to muxer during configuration step"
        synchronized(mediaMuxer) {
            mediaMuxer.writeSampleData(index!!, byteBuffer, bufferInfo)
        }
    }
}
