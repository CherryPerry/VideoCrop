package ru.cherryperry.instavideo.presentation.navigation

import android.net.Uri
import ru.terrakok.cicerone.Screen

/**
 * Open video in external app by it's [Uri].
 * Use with [ru.terrakok.cicerone.Router.navigateTo].
 */
data class OpenVideoScreen(
    val uri: Uri
) : Screen()
