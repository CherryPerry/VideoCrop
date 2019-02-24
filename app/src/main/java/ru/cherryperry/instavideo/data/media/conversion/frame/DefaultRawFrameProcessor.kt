package ru.cherryperry.instavideo.data.media.conversion.frame

/**
 * Default implementation of [RawFrameProcessor].
 * Just copies content of input buffer to output buffer.
 */
class DefaultRawFrameProcessor : RawFrameProcessor {

    override fun process(input: FrameData, output: FrameData) {
        input.byteBuffer.position(input.bufferInfo.offset)
        output.byteBuffer.put(input.byteBuffer)
    }
}
