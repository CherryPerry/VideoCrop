package ru.cherryperry.instavideo.presentation.error

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
import io.mockk.mockk
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import ru.cherryperry.instavideo.FragmentScenario
import ru.cherryperry.instavideo.R
import ru.cherryperry.instavideo.TestInjector
import javax.inject.Inject
import javax.inject.Singleton

@RunWith(AndroidJUnit4::class)
class ErrorFragmentTest {

    @Inject
    lateinit var presenter: ErrorPresenter

    private lateinit var scenario: FragmentScenario<ErrorFragment>

    @Before
    fun before() {
        val fragment = ErrorFragment.newInstance()
        val component = DaggerErrorFragmentTest_TestComponent.builder()
            .module(TestModule())
            .create(fragment) as DaggerErrorFragmentTest_TestComponent
        component.injectTest(this)
        scenario = FragmentScenario.launchInContainer(fragment.javaClass, fragment.arguments,
            component as AndroidInjector<Fragment>)
    }

    @Test
    fun errorIsDisplayed() {
        Espresso.onView(ViewMatchers.withText(R.string.error_text))
            .check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
    }

    @Module
    class TestModule {

        @get:Provides
        @get:Singleton
        val presenter = mockk<ErrorPresenter>(relaxed = true)
    }

    @Singleton
    @Component(modules = [
        AndroidSupportInjectionModule::class,
        TestModule::class
    ])
    interface TestComponent : AndroidInjector<ErrorFragment>, TestInjector<ErrorFragmentTest> {

        @dagger.Component.Builder
        abstract class Builder : AndroidInjector.Builder<ErrorFragment>() {

            abstract fun module(module: TestModule): Builder
        }
    }
}