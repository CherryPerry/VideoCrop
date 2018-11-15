package ru.cherryperry.instavideo.presentation.complete

import android.net.Uri
import androidx.fragment.app.Fragment
import androidx.test.espresso.Espresso
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.ext.junit.runners.AndroidJUnit4
import dagger.Component
import dagger.Module
import dagger.Provides
import dagger.android.AndroidInjector
import dagger.android.support.AndroidSupportInjectionModule
import io.mockk.mockk
import io.mockk.verify
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import ru.cherryperry.instavideo.FragmentScenario
import ru.cherryperry.instavideo.R
import ru.cherryperry.instavideo.TestInjector
import javax.inject.Inject
import javax.inject.Singleton

@RunWith(AndroidJUnit4::class)
class CompleteFragmentTest {

    companion object {
        private val TARGET_URI = Uri.parse("test://target")
    }

    @Inject
    lateinit var presenter: CompletePresenter

    private lateinit var scenario: FragmentScenario<CompleteFragment>

    @Before
    fun before() {
        val fragment = CompleteFragment.newInstance(TARGET_URI)
        val component = DaggerCompleteFragmentTest_TestComponent.builder()
            .module(TestModule())
            .create(fragment) as DaggerCompleteFragmentTest_TestComponent
        component.injectTest(this)
        scenario = FragmentScenario.launchInContainer(fragment.javaClass, fragment.arguments,
            component as AndroidInjector<Fragment>)
    }

    @Test
    fun openResultClick() {
        Espresso.onView(ViewMatchers.withId(R.id.openResult)).perform(ViewActions.click())
        verify { presenter.onOpenResultClick() }
    }

    @Test
    fun convertAnotherClick() {
        Espresso.onView(ViewMatchers.withId(R.id.convertAnother)).perform(ViewActions.click())
        verify { presenter.onConvertAnotherClick() }
    }

    @Test
    fun closeClick() {
        Espresso.onView(ViewMatchers.withId(R.id.close)).perform(ViewActions.click())
        verify { presenter.onCloseClick() }
    }

    @Module
    class TestModule {

        @get:Provides
        @get:Singleton
        val presenter = mockk<CompletePresenter>(relaxed = true)
    }

    @Singleton
    @Component(modules = [
        AndroidSupportInjectionModule::class,
        TestModule::class
    ])
    interface TestComponent : AndroidInjector<CompleteFragment>, TestInjector<CompleteFragmentTest> {

        @Component.Builder
        abstract class Builder : AndroidInjector.Builder<CompleteFragment>() {

            abstract fun module(module: TestModule): Builder
        }
    }
}
