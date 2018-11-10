package ru.cherryperry.instavideo

import android.app.Application
import dagger.BindsInstance
import dagger.Component
import dagger.android.AndroidInjector
import dagger.android.support.AndroidSupportInjectionModule
import dagger.android.support.DaggerApplication
import ru.cherryperry.instavideo.data.DataModule
import ru.cherryperry.instavideo.domain.DomainModule
import ru.cherryperry.instavideo.presentation.ActivityBuilder
import ru.cherryperry.instavideo.presentation.PresentationModule
import javax.inject.Singleton

@Singleton
@Component(modules = [
    AndroidSupportInjectionModule::class,
    ActivityBuilder::class,
    ApplicationModule::class,
    DataModule::class,
    DomainModule::class,
    PresentationModule::class
])
interface ApplicationComponent : AndroidInjector<DaggerApplication> {

    @Component.Builder
    interface Builder {

        @BindsInstance
        fun application(application: Application): Builder

        fun build(): ApplicationComponent
    }
}
