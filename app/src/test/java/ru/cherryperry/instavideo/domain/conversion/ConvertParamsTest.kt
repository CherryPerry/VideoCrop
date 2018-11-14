package ru.cherryperry.instavideo.domain.conversion

import android.graphics.RectF
import android.net.Uri
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert
import org.junit.Rule
import org.junit.Test
import org.junit.rules.ExpectedException
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ConvertParamsTest {

    @Rule
    @JvmField
    val expectedException: ExpectedException = ExpectedException.none()

    @Test
    fun valid() {
        val uri1 = Uri.parse("test://1")
        val uri2 = Uri.parse("test://2")
        val start = 1L
        val end = 2L
        val rectF = RectF(0.1f, 0.2f, 0.3f, 0.4f)
        val params = ConvertParams(uri1, uri2, start, end, rectF)
        Assert.assertEquals(uri1, params.sourceUri)
        Assert.assertEquals(uri2, params.targetUri)
        Assert.assertEquals(start, params.startUs)
        Assert.assertEquals(end, params.endUs)
        Assert.assertEquals(params.sourceRect, rectF)
    }

    fun illegalStartStop() {
        expectedException.expect(IllegalArgumentException::class.java)
        ConvertParams(Uri.EMPTY, Uri.EMPTY, 1, 0, RectF())
    }
}
