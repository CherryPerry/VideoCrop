package ru.cherryperry.instavideo.core

import android.os.Build

/** Check if [Build.VERSION.SDK_INT] >= [level]. */
fun apiLevel(level: Int) = Build.VERSION.SDK_INT >= level

/** Invoke [block] if [Build.VERSION.SDK_INT] >= [level]. */
fun <T> apiLevel(level: Int, block: () -> T): T? {
    return if (Build.VERSION.SDK_INT >= level) {
        block()
    } else {
        null
    }
}
