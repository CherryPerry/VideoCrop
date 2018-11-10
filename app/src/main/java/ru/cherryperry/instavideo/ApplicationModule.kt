package ru.cherryperry.instavideo

import android.app.Application
import android.content.Context
import dagger.Module
import dagger.Provides
import ru.terrakok.cicerone.Cicerone
import ru.terrakok.cicerone.Router
import javax.inject.Singleton

@Module
class ApplicationModule {

    @Provides
    fun context(application: Application): Context = application

    @Provides
    @Singleton
    fun cicerone() = Cicerone.create()

    @Provides
    @Singleton
    fun router(cicerone: Cicerone<Router>) = cicerone.router

    @Provides
    @Singleton
    fun navigationHolder(cicerone: Cicerone<Router>) = cicerone.navigatorHolder
}
