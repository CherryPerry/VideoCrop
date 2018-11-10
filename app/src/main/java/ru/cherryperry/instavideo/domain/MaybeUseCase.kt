package ru.cherryperry.instavideo.domain

import io.reactivex.Maybe

interface MaybeUseCase<Param, Result> {

    fun run(param: Param): Maybe<Result>
}
