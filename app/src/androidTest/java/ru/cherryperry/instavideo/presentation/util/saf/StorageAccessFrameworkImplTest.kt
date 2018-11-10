package ru.cherryperry.instavideo.presentation.util.saf

import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.fragment.app.Fragment
import androidx.test.espresso.intent.Intents
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import ru.cherryperry.instavideo.EmptyAndroidInjector
import ru.cherryperry.instavideo.FragmentScenario
import ru.cherryperry.instavideo.intents

@RunWith(AndroidJUnit4::class)
class StorageAccessFrameworkImplTest {

    private val saf = StorageAccessFrameworkImpl()

    @Test
    fun open() {
        val scenario = FragmentScenario.launch(Fragment::class.java, null, EmptyAndroidInjector())
        intents {
            scenario.onFragment { saf.open(it) }
            val intent = Intents.getIntents().first()
            Assert.assertEquals(Intent.ACTION_OPEN_DOCUMENT, intent.action)
            Assert.assertTrue(intent.categories.any { it == Intent.CATEGORY_OPENABLE })
            Assert.assertEquals("video/*", intent.type)
        }
    }

    @Test
    fun create() {
        val scenario = FragmentScenario.launch(Fragment::class.java, null, EmptyAndroidInjector())
        intents {
            scenario.onFragment { saf.create(it) }
            val intent = Intents.getIntents().first()
            Assert.assertEquals(Intent.ACTION_CREATE_DOCUMENT, intent.action)
            Assert.assertTrue(intent.categories.any { it == Intent.CATEGORY_OPENABLE })
            Assert.assertEquals("video/mp4", intent.type)
        }
    }

    @Test
    fun onActivityResultOpenValid() {
        val intent = Intent().apply { data = Uri.EMPTY }
        val result = saf.onActivityResultOpen(
            StorageAccessFrameworkImpl.OPEN_REQUEST_CODE, Activity.RESULT_OK, intent)
        Assert.assertEquals(intent.data, result)
    }

    @Test
    fun onActivityResultOpenInvalidRequestCode() {
        val intent = Intent().apply { data = Uri.EMPTY }
        val result = saf.onActivityResultOpen(
            StorageAccessFrameworkImpl.CREATE_REQUEST_CODE, Activity.RESULT_OK, intent)
        Assert.assertNull(result)
    }

    @Test
    fun onActivityResultOpenInvalidResultCode() {
        val intent = Intent().apply { data = Uri.EMPTY }
        val result = saf.onActivityResultOpen(
            StorageAccessFrameworkImpl.CREATE_REQUEST_CODE, Activity.RESULT_CANCELED, intent)
        Assert.assertNull(result)
    }

    @Test
    fun onActivityResultOpenInvalidUri() {
        val result = saf.onActivityResultOpen(
            StorageAccessFrameworkImpl.CREATE_REQUEST_CODE, Activity.RESULT_OK, null)
        Assert.assertNull(result)
    }

    @Test
    fun onActivityResultCreateValid() {
        val intent = Intent().apply { data = Uri.EMPTY }
        val result = saf.onActivityResultCreate(
            StorageAccessFrameworkImpl.CREATE_REQUEST_CODE, Activity.RESULT_OK, intent)
        Assert.assertEquals(intent.data, result)
    }

    @Test
    fun onActivityResultCreateInvalidRequestCode() {
        val intent = Intent().apply { data = Uri.EMPTY }
        val result = saf.onActivityResultCreate(
            StorageAccessFrameworkImpl.OPEN_REQUEST_CODE, Activity.RESULT_OK, intent)
        Assert.assertNull(result)
    }

    @Test
    fun onActivityResultCreateInvalidResultCode() {
        val intent = Intent().apply { data = Uri.EMPTY }
        val result = saf.onActivityResultCreate(
            StorageAccessFrameworkImpl.CREATE_REQUEST_CODE, Activity.RESULT_CANCELED, intent)
        Assert.assertNull(result)
    }

    @Test
    fun onActivityResultCreateInvalidUri() {
        val result = saf.onActivityResultCreate(
            StorageAccessFrameworkImpl.CREATE_REQUEST_CODE, Activity.RESULT_OK, null)
        Assert.assertNull(result)
    }
}
