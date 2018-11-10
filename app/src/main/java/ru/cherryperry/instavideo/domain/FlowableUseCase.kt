package ru.cherryperry.instavideo.domain

import io.reactivex.Flowable

interface FlowableUseCase<Param, Result> {

    fun run(param: Param): Flowable<Result>
}
