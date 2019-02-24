package ru.cherryperry.instavideo.data.media.conversion.async

import android.media.MediaCodec
import android.media.MediaFormat
import android.os.Build
import android.os.Handler
import java.nio.ByteBuffer

class CodecHolder(
    private val mediaCodec: MediaCodec,
    private val initialization: Initialization
) {

    private var state: State = State.Uninitialized

    @get:Synchronized
    val currentState: State
        get() = state

    // TODO Remove
    val codec: MediaCodec = mediaCodec

    @Synchronized
    fun configure(callback: MediaCodec.Callback, handler: Handler) {
        if (state != State.Uninitialized) {
            throw IllegalStateException(state.toString())
        }
        // TODO Fix lolipop
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            mediaCodec.setCallback(ErrorHandleCallback(this, callback), handler)
        } else {
            mediaCodec.setCallback(ErrorHandleCallback(this, callback))
        }
        mediaCodec.configure(initialization.mediaFormat, null, null, initialization.flags)
        state = State.Configured
    }

    @Synchronized
    fun start() {
        if (state != State.Configured) {
            throw IllegalStateException(state.toString())
        }
        mediaCodec.start()
        state = State.Running
    }

    @Synchronized
    fun getInputBuffer(index: Int): ByteBuffer {
        if (state != State.Running) {
            throw IllegalStateException(state.toString())
        }
        return mediaCodec.getInputBuffer(index)!!
    }

    @Synchronized
    fun queueInputBuffer(index: Int, offset: Int, size: Int, presentationTimeUs: Long, flags: Int) {
        mediaCodec.queueInputBuffer(index, offset, size, presentationTimeUs, flags)
        if (flags.isEndOfStream()) {
            state = State.EndOfStream
        }
    }

    @Synchronized
    fun queueEndOfStream(index: Int) {
        queueInputBuffer(index, 0, 0, 0, MediaCodec.BUFFER_FLAG_END_OF_STREAM)
    }

    @Synchronized
    fun getOutputBuffer(index: Int): ByteBuffer {
        if (state != State.Running && state != State.EndOfStream) {
            throw IllegalStateException(state.toString())
        }
        return mediaCodec.getOutputBuffer(index)!!
    }

    @Synchronized
    fun releaseOutputBuffer(index: Int) {
        if (state == State.Running || state == State.EndOfStream) {
            mediaCodec.releaseOutputBuffer(index, false)
        }
    }

    @Synchronized
    private fun onError() {
        if (state == State.Uninitialized || state == State.Error) {
            return
        }
        state = State.Error
    }

    @Synchronized
    fun stop() {
        if (state != State.Running && state != State.EndOfStream) {
            throw IllegalStateException(state.toString())
        }
        mediaCodec.stop()
        state = State.Uninitialized
    }

    @Synchronized
    fun release() {
        state = State.Released
    }

    enum class State(
        val validInputs: Boolean,
        val validOutputs: Boolean
    ) {
        Uninitialized(false, false),
        Configured(false, false),
        Running(true, true),
        Flushed(false, false),
        EndOfStream(false, true),
        Error(false, false),
        Released(false, false)
    }

    class Initialization(
        val mediaFormat: MediaFormat,
        val encoder: Boolean
    ) {

        val flags = if (encoder) MediaCodec.CONFIGURE_FLAG_ENCODE else 0
    }

    private class ErrorHandleCallback(
        private val codecHolder: CodecHolder,
        private val callback: MediaCodec.Callback
    ) : MediaCodec.Callback() {

        override fun onOutputBufferAvailable(codec: MediaCodec, index: Int, info: MediaCodec.BufferInfo) {
            callback.onOutputBufferAvailable(codec, index, info)
        }

        override fun onInputBufferAvailable(codec: MediaCodec, index: Int) {
            callback.onInputBufferAvailable(codec, index)
        }

        override fun onOutputFormatChanged(codec: MediaCodec, format: MediaFormat) {
            callback.onOutputFormatChanged(codec, format)
        }

        override fun onError(codec: MediaCodec, exception: MediaCodec.CodecException) {
            callback.onError(codec, exception)
            codecHolder.onError()
        }
    }
}