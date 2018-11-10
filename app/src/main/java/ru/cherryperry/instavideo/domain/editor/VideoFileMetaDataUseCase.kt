package ru.cherryperry.instavideo.domain.editor

import android.net.Uri
import ru.cherryperry.instavideo.domain.SingleUseCase

interface VideoFileMetaDataUseCase : SingleUseCase<Uri, VideoFileMetaData>
