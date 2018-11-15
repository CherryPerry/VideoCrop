package ru.cherryperry.instavideo.presentation.editor

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.RectF
import android.net.Uri
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.matcher.ViewMatchers
import com.google.android.exoplayer2.C
import com.google.android.exoplayer2.Player
import dagger.Component
import dagger.Module
import dagger.Provides
import dagger.android.AndroidInjector
import dagger.android.support.AndroidSupportInjectionModule
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import org.hamcrest.Matchers
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.ExpectedException
import ru.cherryperry.instavideo.AssetsInfo
import ru.cherryperry.instavideo.FragmentScenario
import ru.cherryperry.instavideo.R
import ru.cherryperry.instavideo.TestInjector
import ru.cherryperry.instavideo.onFragmentException
import ru.cherryperry.instavideo.presentation.util.saf.StorageAccessFramework
import ru.cherryperry.instavideo.presentation.util.saf.StorageAccessFrameworkImpl
import ru.cherryperry.instavideo.testResources
import java.io.File
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

class EditorFragmentTest {

    @Rule
    @JvmField
    val expectedException: ExpectedException = ExpectedException.none()

    @Inject
    lateinit var presenter: EditorPresenter
    @Inject
    lateinit var storageAccessFramework: StorageAccessFramework

    private lateinit var scenario: FragmentScenario<EditorFragment>
    private lateinit var sourceUri: Uri

    @Before
    fun before() {
        // test video must be accessible from application context
        val sourceFile = File(ApplicationProvider.getApplicationContext<Context>().filesDir, "source")
        testResources().assets.open("sample_1.mp4").use { inputStream ->
            sourceFile.outputStream().use { outputStream ->
                inputStream.copyTo(outputStream)
            }
        }
        sourceUri = Uri.fromFile(sourceFile)

        // setup
        val fragment = EditorFragment.newInstance(sourceUri)
        val component = DaggerEditorFragmentTest_TestComponent.builder()
            .create(fragment) as DaggerEditorFragmentTest_TestComponent
        component.injectTest(this)
        scenario = FragmentScenario.launchInContainer(fragment.javaClass, fragment.arguments,
            component as AndroidInjector<Fragment>)
    }

    @Test
    fun stateLoading() {
        scenario.onFragment {
            it.showState(EditorView.State.LOADING)
        }
        Espresso.onView(ViewMatchers.withId(R.id.loadingView))
            .check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
        Espresso.onView(ViewMatchers.withId(R.id.errorText))
            .check(ViewAssertions.matches(Matchers.not(ViewMatchers.isDisplayed())))
        Espresso.onView(ViewMatchers.withId(R.id.errorIcon))
            .check(ViewAssertions.matches(Matchers.not(ViewMatchers.isDisplayed())))
        Espresso.onView(ViewMatchers.withId(R.id.timeSelector))
            .check(ViewAssertions.matches(Matchers.not(ViewMatchers.isEnabled())))
        Espresso.onView(ViewMatchers.withId(R.id.apply))
            .check(ViewAssertions.matches(Matchers.not(ViewMatchers.isEnabled())))
    }

    @Test
    fun stateError() {
        scenario.onFragment {
            it.showState(EditorView.State.ERROR)
        }
        Espresso.onView(ViewMatchers.withId(R.id.loadingView))
            .check(ViewAssertions.matches(Matchers.not(ViewMatchers.isDisplayed())))
        Espresso.onView(ViewMatchers.withId(R.id.errorIcon))
            .check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
        Espresso.onView(ViewMatchers.withId(R.id.errorText))
            .check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
        Espresso.onView(ViewMatchers.withId(R.id.timeSelector))
            .check(ViewAssertions.matches(Matchers.not(ViewMatchers.isEnabled())))
        Espresso.onView(ViewMatchers.withId(R.id.apply))
            .check(ViewAssertions.matches(Matchers.not(ViewMatchers.isEnabled())))
    }

    @Test
    fun stateNormal() {
        scenario.onFragment {
            it.showState(EditorView.State.NORMAL)
        }
        Espresso.onView(ViewMatchers.withId(R.id.loadingView))
            .check(ViewAssertions.matches(Matchers.not(ViewMatchers.isDisplayed())))
        Espresso.onView(ViewMatchers.withId(R.id.errorText))
            .check(ViewAssertions.matches(Matchers.not(ViewMatchers.isDisplayed())))
        Espresso.onView(ViewMatchers.withId(R.id.errorIcon))
            .check(ViewAssertions.matches(Matchers.not(ViewMatchers.isDisplayed())))
        Espresso.onView(ViewMatchers.withId(R.id.timeSelector))
            .check(ViewAssertions.matches(ViewMatchers.isEnabled()))
        Espresso.onView(ViewMatchers.withId(R.id.apply))
            .check(ViewAssertions.matches(ViewMatchers.isEnabled()))
    }

    @Test
    fun videoStartsPlayOnSetup() {
        scenario.moveToState(Lifecycle.State.RESUMED)
        scenario.onFragment {
            it.showVideo(sourceUri, C.TIME_UNSET, C.TIME_UNSET)
        }
        // wait for start playing
        // TODO Idling resource?
        Thread.sleep(TimeUnit.SECONDS.toMillis(1))
        // assert
        Espresso.onView(ViewMatchers.withId(R.id.textureView))
            .check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
        scenario.onFragment {
            Assert.assertEquals(Player.STATE_READY, it.checkPlayer().playbackState)
            Assert.assertTrue(it.checkPlayer().currentPosition > 0)
        }
    }

    @Test
    fun videoPauseOnLifecyclePause() {
        scenario.moveToState(Lifecycle.State.RESUMED)
        scenario.onFragment {
            it.showVideo(sourceUri, C.TIME_UNSET, C.TIME_UNSET)
        }
        scenario.onFragment {
            Assert.assertTrue(it.checkPlayer().playWhenReady)
        }
        scenario.moveToState(Lifecycle.State.STARTED)
        scenario.onFragment {
            Assert.assertFalse(it.checkPlayer().playWhenReady)
        }
        scenario.moveToState(Lifecycle.State.RESUMED)
        scenario.onFragment {
            Assert.assertTrue(it.checkPlayer().playWhenReady)
        }
    }

    @Test
    fun applyClick() {
        scenario.onFragment { it.showState(EditorView.State.NORMAL) }
        Espresso.onView(ViewMatchers.withId(R.id.apply)).perform(ViewActions.click())
        verify { storageAccessFramework.create(any()) }
    }

    @Test
    fun onFileCreated() {
        scenario.onFragment {
            it.onActivityResult(StorageAccessFrameworkImpl.CREATE_REQUEST_CODE, Activity.RESULT_OK, Intent())
        }
        // this rect is default one
        verify { presenter.onOutputSelected(Uri.EMPTY, RectF(0f, 0f, 1f, 1f)) }
    }

    @Test
    fun timeSelectionChangeFromStart() {
        scenario.onFragment { it.showState(EditorView.State.NORMAL) }
        Espresso.onView(ViewMatchers.withId(R.id.timeSelector))
            .perform(ViewActions.swipeRight())
        verify { presenter.onSelectionChanged(neq(0f), 1f) }
    }

    @Test
    fun timeSelectionChangeFromEnd() {
        scenario.onFragment { it.showState(EditorView.State.NORMAL) }
        Espresso.onView(ViewMatchers.withId(R.id.timeSelector))
            .perform(ViewActions.swipeLeft())
        verify { presenter.onSelectionChanged(0f, neq(1f)) }
    }

    @Test
    fun timeSelectionLimit() {
        val slot = slot<Float>()
        every { presenter.onSelectionChanged(capture(slot), any()) } returns Unit
        scenario.onFragment {
            it.showState(EditorView.State.NORMAL)
            it.limitSelection(0.3f)
        }
        Espresso.onView(ViewMatchers.withId(R.id.timeSelector))
            .perform(ViewActions.swipeRight())
        verify { presenter.onSelectionChanged(slot.captured, slot.captured) }
    }

    @Test
    fun setVideoRatioIllegalArgumentWidth() {
        expectedException.expect(IllegalArgumentException::class.java)
        scenario.onFragmentException {
            it.setVideoRatio(0, AssetsInfo.SAMPLE_1_HEIGHT)
        }
    }

    @Test
    fun setVideoRatioIllegalArgumentHeight() {
        expectedException.expect(IllegalArgumentException::class.java)
        scenario.onFragmentException {
            it.setVideoRatio(0, AssetsInfo.SAMPLE_1_HEIGHT)
        }
    }

    @Test
    fun showVideoIllegalArgumentBothMustBeUnsetFirst() {
        expectedException.expect(IllegalArgumentException::class.java)
        scenario.onFragmentException {
            it.showVideo(sourceUri, 0, C.TIME_UNSET)
        }
    }

    @Test
    fun showVideoIllegalArgumentBothMustBeUnsetSecond() {
        expectedException.expect(IllegalArgumentException::class.java)
        scenario.onFragmentException {
            it.showVideo(sourceUri, C.TIME_UNSET, AssetsInfo.SAMPLE_1_DURATION_MS)
        }
    }

    @Test
    fun showVideoIllegalArgumentStartAfterEnd() {
        expectedException.expect(IllegalArgumentException::class.java)
        scenario.onFragmentException {
            it.showVideo(sourceUri, 1, 0)
        }
    }

    @Test
    fun showVideoIllegalArgumentNegativeStart() {
        expectedException.expect(IllegalArgumentException::class.java)
        scenario.onFragmentException {
            it.showVideo(sourceUri, -1, 0)
        }
    }

    @Test
    fun showVideoIllegalArgumentNegativeEnd() {
        expectedException.expect(IllegalArgumentException::class.java)
        scenario.onFragmentException {
            it.showVideo(sourceUri, 0, -1)
        }
    }

    @Module
    class TestModule {

        @get:Provides
        @get:Singleton
        val presenter = mockk<EditorPresenter>(relaxed = true)

        @get:Provides
        @get:Singleton
        val storageAccessFramework = mockk<StorageAccessFramework>(relaxed = true) {
            every { onActivityResultCreate(any(), any(), any()) } returns Uri.EMPTY
        }
    }

    @Singleton
    @Component(modules = [
        AndroidSupportInjectionModule::class,
        TestModule::class
    ])
    interface TestComponent : AndroidInjector<EditorFragment>, TestInjector<EditorFragmentTest> {

        @Component.Builder
        abstract class Builder : AndroidInjector.Builder<EditorFragment>() {

            abstract fun module(module: TestModule): Builder
        }
    }
}
