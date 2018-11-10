package ru.cherryperry.instavideo.presentation.navigation

import android.graphics.RectF
import android.net.Uri
import androidx.fragment.app.Fragment
import ru.cherryperry.instavideo.presentation.conversion.ConversionFragment
import ru.cherryperry.instavideo.presentation.navigation.cicerone.SupportAppScreen

data class ConversionScreen(
    private val inputUri: Uri,
    private val outputUri: Uri,
    private val startUs: Long,
    private val endUs: Long,
    private val sourceRect: RectF
) : SupportAppScreen() {

    override fun getFragment(): Fragment = ConversionFragment
        .newInstance(inputUri, outputUri, startUs, endUs, sourceRect)
}
