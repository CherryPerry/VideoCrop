package ru.cherryperry.instavideo.domain.conversion

import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable
import org.junit.Test

class RxVideoConverterCallbackTest {

    @Test
    fun valid() {
        Flowable
            .create<Float>({
                val callback = ConvertUseCaseImpl.RxVideoConverterCallback(it)
                callback.onProgressChanged(0f)
                callback.onProgressChanged(0.5f)
                callback.onProgressChanged(1f)
            }, BackpressureStrategy.LATEST)
            .test()
            .awaitCount(3)
            .assertValues(0f, 0.5f, 1f)
            .assertNotComplete()
            .dispose()
    }

    @Test
    fun argumentIsLowerThanZero() {
        Flowable
            .create<Float>({
                val callback = ConvertUseCaseImpl.RxVideoConverterCallback(it)
                callback.onProgressChanged(-1f)
            }, BackpressureStrategy.LATEST)
            .test()
            .await()
            .assertError(IllegalArgumentException::class.java)
            .dispose()
    }

    @Test
    fun argumentIsHigherThanOne() {
        Flowable
            .create<Float>({
                val callback = ConvertUseCaseImpl.RxVideoConverterCallback(it)
                callback.onProgressChanged(2f)
            }, BackpressureStrategy.LATEST)
            .test()
            .await()
            .assertError(IllegalArgumentException::class.java)
            .dispose()
    }
}
