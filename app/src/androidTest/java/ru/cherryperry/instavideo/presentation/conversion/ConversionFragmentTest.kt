package ru.cherryperry.instavideo.presentation.conversion

import android.graphics.RectF
import android.net.Uri
import android.widget.ProgressBar
import androidx.fragment.app.Fragment
import androidx.test.espresso.Espresso
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.ext.junit.runners.AndroidJUnit4
import dagger.Component
import dagger.Module
import dagger.Provides
import dagger.android.AndroidInjector
import dagger.android.support.AndroidSupportInjectionModule
import org.hamcrest.Matchers
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito
import ru.cherryperry.instavideo.FragmentScenario
import ru.cherryperry.instavideo.R
import ru.cherryperry.instavideo.TestInjector
import ru.cherryperry.instavideo.matcher
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
    fun errorState() {
        scenario.onFragment {
            it.showState(ConversionScreenErrorState)
        }
        Espresso.onView(ViewMatchers.withId(R.id.loadingGroup))
            .check(ViewAssertions.matches(Matchers.not(ViewMatchers.isDisplayed())))
        Espresso.onView(ViewMatchers.withId(R.id.errorGroup))
            .check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
        Espresso.onView(ViewMatchers.withId(R.id.completeGroup))
            .check(ViewAssertions.matches(Matchers.not(ViewMatchers.isDisplayed())))
    }

    @Test
    fun loadingState() {
        scenario.onFragment {
            it.showState(ConversionScreenProgressState(0.5f))
        }
        Espresso.onView(ViewMatchers.withId(R.id.loadingGroup))
            .check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
        Espresso.onView(ViewMatchers.withId(R.id.progressBar))
            .check(ViewAssertions.matches(matcher {
                (it as ProgressBar).let { progressBar ->
                    progressBar.progress / progressBar.max.toFloat() == 0.5f
                }
            }))
        Espresso.onView(ViewMatchers.withId(R.id.errorGroup))
            .check(ViewAssertions.matches(Matchers.not(ViewMatchers.isDisplayed())))
        Espresso.onView(ViewMatchers.withId(R.id.completeGroup))
            .check(ViewAssertions.matches(Matchers.not(ViewMatchers.isDisplayed())))
    }

    @Test
    fun completedState() {
        scenario.onFragment {
            it.showState(ConversionScreenCompleteState)
        }
        Espresso.onView(ViewMatchers.withId(R.id.loadingGroup))
            .check(ViewAssertions.matches(Matchers.not(ViewMatchers.isDisplayed())))
        Espresso.onView(ViewMatchers.withId(R.id.errorGroup))
            .check(ViewAssertions.matches(Matchers.not((ViewMatchers.isDisplayed()))))
        Espresso.onView(ViewMatchers.withId(R.id.completeGroup))
            .check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
    }

    @Module
    class TestModule {

        @get:Provides
        val presenter: ConversionPresenter = Mockito.mock(ConversionPresenter::class.java)
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
