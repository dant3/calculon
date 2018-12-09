package com.github.dant3.settingsdsl

import android.annotation.SuppressLint
import android.content.SharedPreferences
import kotlin.reflect.KProperty

interface PreferenceAccessor<T> {
    val parent: SharedPreferences

    fun getValue(): T
    fun SharedPreferences.Editor.putValue(newValue: T): SharedPreferences.Editor

    fun setValue(value: T) { setValue(value, flush = false) }
    @SuppressLint("CommitPrefEdits")
    fun setValue(newValue: T, flush: Boolean) {
        val editor = parent.edit()
        editor.putValue(newValue)
        when (flush) {
            true -> editor.commit()
            else -> editor.apply()
        }
    }
}

inline operator fun <T> PreferenceAccessor<T>.getValue(thisRef: Any?, property: KProperty<*>): T = getValue()
inline operator fun <T> PreferenceAccessor<T>.setValue(thisRef: Any?, property: KProperty<*>, value: T) { setValue(value) }
