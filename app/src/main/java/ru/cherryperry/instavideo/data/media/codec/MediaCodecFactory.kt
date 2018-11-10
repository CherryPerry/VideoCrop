package ru.cherryperry.instavideo.data.media.codec

import android.media.MediaCodec
import android.media.MediaCodecInfo
import android.media.MediaCodecList
import android.media.MediaFormat
import android.os.Build
import com.google.android.exoplayer2.util.MimeTypes
import ru.cherryperry.instavideo.core.apiLevel

object MediaCodecFactory {

    private const val DEFAULT_SIZE = 512
    private const val DEFAULT_FPS = 30
    private const val DEFAULT_VIDEO_BITRATE = 2 * 1024 * 1024
    private const val DEFAULT_I_FRAME_INTERVAL = 3
    private const val DEFAULT_SAMPLE_RATE = 48000
    private const val DEFAULT_CHANNEL_COUNT = 2
    private const val DEFAULT_AUDIO_BITRATE = 256 * 1024

    private val mediaCodecList = MediaCodecList(MediaCodecList.REGULAR_CODECS)

    fun createVideoEncoder(fps: Int = DEFAULT_FPS): MediaCodec {
        val mediaFormat = MediaFormat.createVideoFormat(MimeTypes.VIDEO_H264, DEFAULT_SIZE, DEFAULT_SIZE)
        val name = mediaCodecList.findEncoderForFormat(mediaFormat)
        val encoder = MediaCodec.createByCodecName(name)
        mediaFormat.setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420Flexible)
        mediaFormat.setInteger(MediaFormat.KEY_BITRATE_MODE, MediaCodecInfo.EncoderCapabilities.BITRATE_MODE_CBR)
        mediaFormat.setInteger(MediaFormat.KEY_BIT_RATE, DEFAULT_VIDEO_BITRATE)
        mediaFormat.setInteger(MediaFormat.KEY_FRAME_RATE, fps)
        mediaFormat.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, DEFAULT_I_FRAME_INTERVAL)
        mediaFormat.setInteger(MediaFormat.KEY_PROFILE, MediaCodecInfo.CodecProfileLevel.AVCProfileBaseline)
        apiLevel(Build.VERSION_CODES.M) {
            mediaFormat.setInteger(MediaFormat.KEY_LEVEL, MediaCodecInfo.CodecProfileLevel.AVCLevel31)
        }
        encoder.configure(mediaFormat, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE)
        return encoder
    }

    fun createAudioEncoder(sampleRate: Int = DEFAULT_SAMPLE_RATE, channelCount: Int = DEFAULT_CHANNEL_COUNT): MediaCodec {
        val mediaFormat = MediaFormat.createAudioFormat(MimeTypes.AUDIO_AAC, sampleRate, channelCount)
        mediaFormat.setInteger(MediaFormat.KEY_AAC_PROFILE, MediaCodecInfo.CodecProfileLevel.AACObjectLC)
        mediaFormat.setInteger(MediaFormat.KEY_BIT_RATE, DEFAULT_AUDIO_BITRATE)
        val name = mediaCodecList.findEncoderForFormat(mediaFormat)
        val encoder = MediaCodec.createByCodecName(name)
        encoder.configure(mediaFormat, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE)
        return encoder
    }

    fun createDecoderAndConfigure(mediaFormat: MediaFormat): MediaCodec {
        val name = mediaCodecList.findDecoderForFormat(mediaFormat)
        val decoder = MediaCodec.createByCodecName(name)
        decoder.configure(mediaFormat, null, null, 0)
        return decoder
    }
}
