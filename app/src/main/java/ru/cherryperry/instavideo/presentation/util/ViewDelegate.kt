package ru.cherryperry.instavideo.presentation.util

import android.app.Activity
import android.view.View
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import kotlin.reflect.KProperty

class ViewDelegateReset {

    private val list = ArrayList<ViewDelegate<*>>()

    internal fun register(viewDelegate: ViewDelegate<*>) {
        list.add(viewDelegate)
    }

    fun onDestroyView() {
        list.forEach { it.onDestroyView() }
    }
}

class ViewDelegate<out T : View?>(
    viewId: Int,
    viewDelegateReset: ViewDelegateReset? = null
) {
    private var value: T? = null
    private val id = viewId

    init {
        viewDelegateReset?.register(this)
    }

    operator fun getValue(thisRef: Any?, property: KProperty<*>): T {
        value?.let { return it }
        val newValue = when (thisRef) {
            is Activity -> thisRef.findViewById<T>(id)
            is Fragment -> thisRef.view!!.findViewById<T>(id)
            is View -> thisRef.findViewById<T>(id)
            is RecyclerView.ViewHolder -> thisRef.itemView.findViewById<T>(id)
            else -> throw IllegalStateException("Other thisRefs not implemented")
        }
        value = newValue
        return newValue
    }

    internal fun onDestroyView() {
        value = null
    }
}
