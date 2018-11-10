package ru.cherryperry.instavideo.domain.conversion

import android.content.Context
import androidx.annotation.FloatRange
import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable
import io.reactivex.FlowableEmitter
import io.reactivex.schedulers.Schedulers
import ru.cherryperry.instavideo.core.illegalArgument
import ru.cherryperry.instavideo.data.media.conversion.UriMediaExtractorSource
import ru.cherryperry.instavideo.data.media.conversion.VideoConverter
import javax.inject.Inject
import javax.inject.Provider

class ConvertUseCaseImpl @Inject constructor(
    private val converter: Provider<VideoConverter>,
    private val fileProxy: FileProxy,
    private val context: Context
) : ConvertUseCase {

    override fun run(param: ConvertParams): Flowable<Float> = Flowable
        .create<Float>({ emitter ->
            emitter.onNext(0f)
            converter.get().use {
                it.process(
                    UriMediaExtractorSource(param.sourceUri, context),
                    param.startUs,
                    param.endUs,
                    param.sourceRect,
                    fileProxy.proxyFile.absolutePath,
                    RxVideoConverterCallback(emitter)
                )
                fileProxy.copyProxyToResult(param.targetUri)
            }
            emitter.onNext(1f)
            emitter.onComplete()
        }, BackpressureStrategy.LATEST)
        .subscribeOn(Schedulers.io())

    class RxVideoConverterCallback(
        private val emitter: FlowableEmitter<Float>
    ) : VideoConverter.Callback {

        override fun onProgressChanged(@FloatRange(from = 0.0, to = 1.0) progress: Float) {
            (progress < 0f || progress > 1f) illegalArgument "Progress is not in range [0,1]"
            emitter.onNext(progress)
        }
    }
}
