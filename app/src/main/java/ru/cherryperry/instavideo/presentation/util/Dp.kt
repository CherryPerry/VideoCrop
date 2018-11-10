package ru.cherryperry.instavideo.presentation.util

import android.content.Context

infix fun Int.dp(context: Context): Float = this * context.resources.displayMetrics.density
