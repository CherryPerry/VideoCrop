package ru.cherryperry.instavideo.presentation.conversion

import dagger.Module
import dagger.Provides
import dagger.Subcomponent
import dagger.android.AndroidInjector
import javax.inject.Qualifier

@Module
class ConversionFragmentModule {

    @Provides
    @TargetUri
    fun targetUri(fragment: ConversionFragment) = ConversionFragment.targetUri(fragment.arguments!!)

    @Provides
    @SourceUri
    fun sourceUri(fragment: ConversionFragment) = ConversionFragment.sourceUri(fragment.arguments!!)

    @Provides
    @StartTime
    fun start(fragment: ConversionFragment) = ConversionFragment.start(fragment.arguments!!)

    @Provides
    @EndTime
    fun end(fragment: ConversionFragment) = ConversionFragment.end(fragment.arguments!!)

    @Provides
    fun sourceRect(fragment: ConversionFragment) = ConversionFragment.sourceRect(fragment.arguments!!)
}

@Subcomponent(modules = [ConversionFragmentModule::class])
interface ConversionFragmentSubcomponent : AndroidInjector<ConversionFragment> {

    @Subcomponent.Builder
    abstract class Builder : AndroidInjector.Builder<ConversionFragment>()
}

@Qualifier
@Retention(AnnotationRetention.SOURCE)
annotation class TargetUri

@Qualifier
@Retention(AnnotationRetention.SOURCE)
annotation class SourceUri

@Qualifier
@Retention(AnnotationRetention.SOURCE)
annotation class StartTime

@Qualifier
@Retention(AnnotationRetention.SOURCE)
annotation class EndTime
