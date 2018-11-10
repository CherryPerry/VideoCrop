package ru.cherryperry.instavideo

import android.content.Context
import android.content.res.Resources
import androidx.fragment.app.Fragment
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.intent.Intents
import dagger.android.AndroidInjector
import org.hamcrest.BaseMatcher
import org.hamcrest.Description

/**
 * Executes block between [Intents.init] and [Intents.release] calls.
 */
inline fun intents(block: () -> Unit) {
    Intents.init()
    try {
        block()
    } finally {
        Intents.release()
    }
}

/** Like [AndroidInjector] but for tests. */
interface TestInjector<T> {

    fun injectTest(test: T)
}

/** Empty implementation of [AndroidInjector] for [FragmentScenario]. */
class EmptyAndroidInjector : AndroidInjector<Fragment> {

    override fun inject(instance: Fragment?) {
        // nothing
    }
}

/** Info about assets. */
object AssetsInfo {

    const val SAMPLE_1_DURATION_MS = 5312L
}

/** [Resources] of androidTest folder. */
fun testResources(): Resources = ApplicationProvider.getApplicationContext<Context>()
    .packageManager
    .getResourcesForApplication("ru.cherryperry.instavideo.test")

fun <T> matcher(block: (T) -> Boolean) = object : BaseMatcher<T>() {

    override fun describeTo(description: Description?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun matches(item: Any?): Boolean {
        return block(item as T)
    }
}
