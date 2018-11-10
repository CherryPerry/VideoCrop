package ru.cherryperry.instavideo.core

infix fun Boolean.illegalArgument(message: String) {
    if (this) {
        throw IllegalArgumentException(message)
    }
}

infix fun Boolean.illegalState(message: String) {
    if (this) {
        throw IllegalStateException(message)
    }
}
