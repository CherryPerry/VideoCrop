package ru.cherryperry.instavideo

import android.content.Context
import android.content.res.Resources
import androidx.fragment.app.Fragment
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.intent.Intents
import dagger.android.AndroidInjector
import java.util.concurrent.atomic.AtomicReference

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
    const val SAMPLE_1_WIDTH = 1280L
    const val SAMPLE_1_HEIGHT = 720L

}

/** [Resources] of androidTest folder. */
fun testResources(): Resources = ApplicationProvider.getApplicationContext<Context>()
    .packageManager
    .getResourcesForApplication("ru.cherryperry.instavideo.test")

fun <T : Fragment> FragmentScenario<T>.onFragmentException(block: (T) -> Unit) {
    val ref = AtomicReference<Throwable>()
    this.onFragment {
        try {
            block(it)
        } catch (throwable: Throwable) {
            ref.set(throwable)
        }
    }
    ref.get()?.let { throw it }
}
