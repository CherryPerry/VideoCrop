package ru.cherryperry.instavideo.presentation.util

import android.view.View
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ViewExtTest {

    private lateinit var view: View

    @Before
    fun before() {
        view = View(ApplicationProvider.getApplicationContext())
        view.measure(
            View.MeasureSpec.makeMeasureSpec(View.MeasureSpec.EXACTLY, 30),
            View.MeasureSpec.makeMeasureSpec(View.MeasureSpec.EXACTLY, 20))
        view.layout(10, 20, 40, 40)
    }

    @Test
    fun centerX() {
        Assert.assertEquals(25, view.centerX)
    }

    @Test
    fun centerY() {
        Assert.assertEquals(30, view.centerY)
    }
}
