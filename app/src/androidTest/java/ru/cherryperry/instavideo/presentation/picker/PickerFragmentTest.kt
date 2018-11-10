package ru.cherryperry.instavideo.presentation.picker

import android.app.Activity
import android.content.Intent
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
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito
import ru.cherryperry.instavideo.FragmentScenario
import ru.cherryperry.instavideo.R
import ru.cherryperry.instavideo.TestInjector
import ru.cherryperry.instavideo.presentation.util.saf.StorageAccessFramework
import javax.inject.Inject

@RunWith(AndroidJUnit4::class)
class PickerFragmentTest {

    @Inject
    lateinit var storageAccessFramework: StorageAccessFramework
    @Inject
    lateinit var presenter: PickerPresenter

    private lateinit var scenario: FragmentScenario<PickerFragment>

    @Before
    fun before() {
        val fragment = PickerFragment.newInstance()
        val component = DaggerPickerFragmentTest_TestComponent.builder()
            .module(TestModule())
            .create(fragment) as DaggerPickerFragmentTest_TestComponent
        component.injectTest(this)
        scenario = FragmentScenario.launchInContainer(fragment.javaClass, fragment.arguments,
            component as AndroidInjector<Fragment>)
    }

    @Test
    fun pickerButtonClick() {
        Espresso.onView(ViewMatchers.withText(R.string.picker_select_video)).perform(ViewActions.click())
        scenario.onFragment {
            Mockito.verify(storageAccessFramework).open(it)
        }
    }

    @Test
    fun storageAccessFrameworkResult() {
        val requestCode = 1
        val resultIntent = Intent().apply { data = Uri.EMPTY }
        Mockito.`when`(storageAccessFramework.onActivityResultOpen(requestCode, Activity.RESULT_OK, resultIntent))
            .thenReturn(Uri.EMPTY)
        scenario.onFragment {
            it.onActivityResult(requestCode, Activity.RESULT_OK, resultIntent)
            Mockito.verify(presenter).onVideoSelected(Uri.EMPTY)
        }
    }

    @Module
    class TestModule {

        @get:Provides
        val storageAccessFramework: StorageAccessFramework = Mockito.mock(StorageAccessFramework::class.java)
        @get:Provides
        val presenter: PickerPresenter = Mockito.mock(PickerPresenter::class.java)
    }

    @Component(modules = [
        AndroidSupportInjectionModule::class,
        TestModule::class
    ])
    interface TestComponent : AndroidInjector<PickerFragment>, TestInjector<PickerFragmentTest> {

        @dagger.Component.Builder
        abstract class Builder : AndroidInjector.Builder<PickerFragment>() {

            abstract fun module(module: TestModule): Builder
        }
    }
}