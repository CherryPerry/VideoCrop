package ru.cherryperry.instavideo.domain.editor

class InvalidVideoFileException(
    exception: Exception
) : Exception("Provided file is not video file or video file of unsupported format", exception)
