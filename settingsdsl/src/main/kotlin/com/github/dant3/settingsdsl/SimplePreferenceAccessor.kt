package com.github.dant3.settingsdsl

import android.content.SharedPreferences

interface SimplePreferenceAccessor<T> : PreferenceAccessor<T> {
    val key: String

    fun <U> map(read: (T) -> U, write: (U) -> T): SimplePreferenceAccessor<U> {
        return object : SimplePreferenceAccessor<U> {
            override val key: String = this@SimplePreferenceAccessor.key
            override val parent: SharedPreferences = this@SimplePreferenceAccessor.parent

            override fun getValue(): U = read(this@SimplePreferenceAccessor.getValue())

            override fun SharedPreferences.Editor.putValue(newValue: U) = putValue(write(newValue))
        }
    }
}


internal fun <T : Any> SimplePreferenceAccessor<T?>.nonNull(): SimplePreferenceAccessor<T> = map(
        read = { it ?: throw NullPointerException("Value of key $key is null") },
        write = { it }
)

fun SharedPreferences.optionalStringPreference(key: String, defaultValue: String? = null) = object : SimplePreferenceAccessor<String?> {
    override val key: String = key
    override val parent: SharedPreferences = this@optionalStringPreference

    override fun getValue(): String? = parent.getString(key, defaultValue)
    override fun SharedPreferences.Editor.putValue(newValue: String?) = putString(key, newValue)
}

fun <T> SharedPreferences.optionalObjectPreference(read: (SharedPreferences) -> T?,
                                                   write: SharedPreferences.Editor.(T?) -> SharedPreferences.Editor) = object : PreferenceAccessor<T?> {
    override val parent: SharedPreferences = this@optionalObjectPreference
    override fun getValue(): T? = read(parent)
    override fun SharedPreferences.Editor.putValue(newValue: T?): SharedPreferences.Editor = write(newValue)
}

fun <T> SharedPreferences.objectPreference(read: (SharedPreferences) -> T,
                                           write: SharedPreferences.Editor.(T) -> SharedPreferences.Editor) = object : PreferenceAccessor<T> {
    override val parent: SharedPreferences = this@objectPreference
    override fun getValue(): T = read(parent)
    override fun SharedPreferences.Editor.putValue(newValue: T): SharedPreferences.Editor = write(newValue)
}

fun SharedPreferences.stringPreference(key: String, defaultValue: String): SimplePreferenceAccessor<String> =
        optionalStringPreference(key, defaultValue).nonNull()

fun <T> SharedPreferences.mappedObject(key: String, defaultValue: T, write: (T) -> String, read: (String) -> T?): SimplePreferenceAccessor<T> =
        stringPreference(key, write(defaultValue)).map({
            read(it) ?: defaultValue
        }, write)

fun SharedPreferences.intPreference(key: String, defaultValue: Int) = object : SimplePreferenceAccessor<Int> {
    override val key: String = key
    override val parent: SharedPreferences = this@intPreference

    override fun getValue() = parent.getInt(key, defaultValue)
    override fun SharedPreferences.Editor.putValue(newValue: Int) = putInt(key, newValue)
}

fun SharedPreferences.booleanPreference(key: String, defaultValue: Boolean) = object : SimplePreferenceAccessor<Boolean> {
    override val key: String = key
    override val parent: SharedPreferences = this@booleanPreference

    override fun getValue() = parent.getBoolean(key, defaultValue)
    override fun SharedPreferences.Editor.putValue(newValue: Boolean) = putBoolean(key, newValue)
}

fun SharedPreferences.optionalBooleanPreference(key: String) = object : SimplePreferenceAccessor<Boolean?> {
    override val key: String = key
    override val parent: SharedPreferences = this@optionalBooleanPreference

    override fun getValue(): Boolean? =
            if (parent.contains(key)) parent.getBoolean(key, false) else null

    override fun SharedPreferences.Editor.putValue(newValue: Boolean?): SharedPreferences.Editor =
            if (newValue == null) {
                remove(key)
            } else {
                putBoolean(key, newValue)
            }
}

fun SharedPreferences.floatPreference(key: String, defaultValue: Float) = object : SimplePreferenceAccessor<Float> {
    override val key: String = key
    override val parent: SharedPreferences = this@floatPreference

    override fun getValue() = parent.getFloat(key, defaultValue)
    override fun SharedPreferences.Editor.putValue(newValue: Float) = putFloat(key, newValue)
}

fun SharedPreferences.doubleCompatPreference(key: String, defaultValue: Double) = object : SimplePreferenceAccessor<Double> {
    override val key: String = key
    override val parent: SharedPreferences = this@doubleCompatPreference

    override fun getValue() = parent.getString(key, null).toDoubleOrNull() ?: defaultValue
    override fun SharedPreferences.Editor.putValue(newValue: Double) = putString(key, newValue.toString())
}

fun SharedPreferences.longPreference(key: String, defaultValue: Long) = object : SimplePreferenceAccessor<Long> {
    override val key: String = key
    override val parent: SharedPreferences = this@longPreference

    override fun getValue() = parent.getLong(key, defaultValue)
    override fun SharedPreferences.Editor.putValue(newValue: Long) = putLong(key, newValue)
}

fun SharedPreferences.optionalStringSetPreference(key: String, defaultValue: Set<String>? = null) = object : SimplePreferenceAccessor<Set<String>?> {
    override val key: String = key
    override val parent: SharedPreferences = this@optionalStringSetPreference

    override fun getValue() = parent.getStringSet(key, defaultValue)
    override fun SharedPreferences.Editor.putValue(newValue: Set<String>?) = putStringSet(key, newValue)
}

fun SharedPreferences.stringSetPreference(key: String, defaultValue: Set<String> = emptySet()): SimplePreferenceAccessor<Set<String>> =
        optionalStringSetPreference(key, defaultValue).nonNull()