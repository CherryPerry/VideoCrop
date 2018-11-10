package ru.cherryperry.instavideo.domain

import io.reactivex.Single

interface SingleUseCase<Param, Result> {

    fun run(param: Param): Single<Result>
}
