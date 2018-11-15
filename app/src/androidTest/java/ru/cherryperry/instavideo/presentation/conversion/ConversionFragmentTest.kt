package ru.cherryperry.instavideo.presentation.conversion

import android.graphics.RectF
import android.net.Uri
import android.view.View
import android.widget.ProgressBar
import androidx.fragment.app.Fragment
import androidx.test.espresso.Espresso
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.matcher.BoundedMatcher
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.ext.junit.runners.AndroidJUnit4
import dagger.Component
import dagger.Module
import dagger.Provides
import dagger.android.AndroidInjector
import dagger.android.support.AndroidSupportInjectionModule
import io.mockk.mockk
import org.hamcrest.Description
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.ExpectedException
import org.junit.runner.RunWith
import ru.cherryperry.instavideo.FragmentScenario
import ru.cherryperry.instavideo.R
import ru.cherryperry.instavideo.TestInjector
import ru.cherryperry.instavideo.onFragmentException
import javax.inject.Inject

@RunWith(AndroidJUnit4::class)
class ConversionFragmentTest {

    companion object {
        private val SOURCE_URI = Uri.parse("test://source")
        private val TARGET_URI = Uri.parse("test://target")
        private const val START = 10L
        private const val END = 20L
        private val RECT_F = RectF(0.1f, 0.2f, 0.3f, 0.4f)
    }

    @Rule
    @JvmField
    val expectedException: ExpectedException = ExpectedException.none()

    @Inject
    lateinit var presenter: ConversionPresenter

    private lateinit var scenario: FragmentScenario<ConversionFragment>

    @Before
    fun before() {
        val fragment = ConversionFragment.newInstance(SOURCE_URI, TARGET_URI, START, END, RECT_F)
        val component = DaggerConversionFragmentTest_TestComponent.builder()
            .module(TestModule())
            .create(fragment) as DaggerConversionFragmentTest_TestComponent
        component.injectTest(this)
        scenario = FragmentScenario.launchInContainer(fragment.javaClass, fragment.arguments,
            component as AndroidInjector<Fragment>)
    }

    @Test
    fun showProgress() {
        scenario.onFragment {
            it.showProgress(0.5f)
        }
        Espresso.onView(ViewMatchers.withId(R.id.progressBar))
            .check(ViewAssertions.matches(ProgressBarMatcher(50)))
    }

    @Test
    fun showProgressInvalidProgressLowerThanZero() {
        expectedException.expect(IllegalArgumentException::class.java)
        scenario.onFragmentException {
            it.showProgress(-0.1f)
        }
    }

    @Test
    fun showProgressInvalidProgressHigherThanOne() {
        expectedException.expect(IllegalArgumentException::class.java)
        scenario.onFragmentException {
            it.showProgress(1.1f)
        }
    }

    class ProgressBarMatcher(
        private val progressValue: Int
    ) : BoundedMatcher<View, ProgressBar>(ProgressBar::class.java) {

        override fun matchesSafely(item: ProgressBar): Boolean = item.progress == progressValue

        override fun describeTo(description: Description) {
            description.appendText("with progress value:").appendValue(progressValue)
        }
    }

    @Module
    class TestModule {

        @get:Provides
        val presenter = mockk<ConversionPresenter>(relaxed = true)
    }

    @Component(modules = [
        AndroidSupportInjectionModule::class,
        TestModule::class
    ])
    interface TestComponent : AndroidInjector<ConversionFragment>, TestInjector<ConversionFragmentTest> {

        @dagger.Component.Builder
        abstract class Builder : AndroidInjector.Builder<ConversionFragment>() {

            abstract fun module(module: TestModule): Builder
        }
    }
}
