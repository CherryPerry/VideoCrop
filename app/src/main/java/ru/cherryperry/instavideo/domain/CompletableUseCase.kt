package ru.cherryperry.instavideo.domain

import io.reactivex.Completable

interface CompletableUseCase<Param> {

    fun run(param: Param): Completable
}
