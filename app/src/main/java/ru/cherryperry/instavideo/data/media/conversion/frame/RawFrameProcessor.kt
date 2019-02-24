package ru.cherryperry.instavideo.data.media.conversion.frame

/**
 * Frame processor allows you to do any kind of transformation on each frame.
 * Be careful with format of raw data.
 */
interface RawFrameProcessor {

    fun process(input: FrameData, output: FrameData)
}
