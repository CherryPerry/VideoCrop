package ru.cherryperry.instavideo.core

import android.os.Build
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AndroidVersionTest {

    @Test
    fun apiLevelTrue() {
        Assert.assertTrue(apiLevel(Build.VERSION.SDK_INT))
    }

    @Test
    fun apiLevelFalse() {
        Assert.assertFalse(apiLevel(Build.VERSION.SDK_INT + 1))
    }

    @Test
    fun apiLevelBlockTrue() {
        Assert.assertNotNull(apiLevel(Build.VERSION.SDK_INT) { true })
    }

    @Test
    fun apiLevelBlockFalse() {
        Assert.assertNull(apiLevel(Build.VERSION.SDK_INT + 1) { true })
    }
}
