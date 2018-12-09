package com.github.dant3.settingsdsl

import android.content.Context
import android.support.v14.preference.SwitchPreference
import android.support.v17.preference.LeanbackPreferenceFragment
import android.support.v7.preference.*
import android.util.TypedValue
import com.github.dant3.rxdroid.MainThreadScheduler
import com.github.dant3.rxvar.RxVar
import io.reactivex.disposables.CompositeDisposable
import kotlin.reflect.KMutableProperty0



typealias Configurator<T> = T.() -> Unit
typealias DynamicConfigurator<T, U> = T.(U) -> Unit


internal class Configuration<in T, in U> private constructor(val isDynamic: Boolean, private val impl: T.(U) -> Unit) {
    fun apply(preference: T, value: U) { impl(preference, value) }

    companion object {
        fun <T> static(configurator: Configurator<T>): Configuration<T, Any?> = Configuration(false) { configurator.invoke(this) }
        fun <T, U> dynamic(configurator: DynamicConfigurator<T, U>): Configuration<T, U> = Configuration(true, configurator)
    }
}


fun LeanbackPreferenceFragment.screen(title: Int, configurator: Configurator<PreferenceScreen>? = null) = screen(resources.getString(title), configurator)

fun LeanbackPreferenceFragment.screen(title: CharSequence, configurator: Configurator<PreferenceScreen>? = null): PreferenceScreen =
        preferenceManager.createPreferenceScreen(this.activity).apply {
            this.title = title
            configurator?.invoke(this)
        }


fun PreferenceFragmentCompat.screen(title: Int, configurator: Configurator<PreferenceScreen>? = null) = screen(resources.getString(title), configurator)

fun PreferenceFragmentCompat.screen(title: CharSequence, configurator: Configurator<PreferenceScreen>? = null): PreferenceScreen =
        preferenceManager.createPreferenceScreen(this.context).apply {
            this.title = title
            configurator?.invoke(this)
        }

fun <T : PreferenceGroup> T.category(title: Int, configurator: Configurator<PreferenceCategory>? = null) = category(context.resources.getString(title), configurator)

fun <T : PreferenceGroup> T.category(title: CharSequence, configurator: Configurator<PreferenceCategory>? = null): PreferenceCategory =
        addPref(configurator) {
            PreferenceCategory(it).apply {
                this.title = title
            }
        }

fun <T : PreferenceGroup> T.screen(title: Int, configurator: Configurator<PreferenceScreen>? = null) = screen(context.resources.getString(title), configurator)

fun <T : PreferenceGroup> T.screen(title: CharSequence, configurator: Configurator<PreferenceScreen>? = null): PreferenceScreen {
    val screen = preferenceManager.createPreferenceScreen(context)
    screen.title = title
    addPreference(screen)
    configurator?.invoke(screen)
    return screen
}

val <T : PreferenceGroup> T.checkBox
    get() = AccessorPreferenceDsl<CheckBoxPreference, Boolean>(this) { accessor, configuration ->
        object : CheckBoxPreference(this) {
            private val subscription = CompositeDisposable()

            init {
                onPreferenceChangeListener = accessor.value.update { it as Boolean }
                applyConfiguration(configuration, isChecked, onChange = false)
            }

            override fun onAttached() {
                super.onAttached()
                subscription.add(accessor.value.bind(MainThreadScheduler) {
                    isChecked = it
                    applyConfiguration(configuration, it, onChange = true)
                })
            }

            override fun onDetached() {
                super.onDetached()
                subscription.clear()
            }
        }
    }

val <T : PreferenceGroup> T.switch
    get() = AccessorPreferenceDsl<SwitchPreference, Boolean>(this) { accessor, configuration ->
        object : SwitchPreference(this) {
            private val subscription = CompositeDisposable()

            init {
                onPreferenceChangeListener = accessor.value.update { it as Boolean }
                applyConfiguration(configuration, isChecked, onChange = false)
            }

            override fun onAttached() {
                super.onAttached()
                subscription.add(accessor.value.bind(MainThreadScheduler) {
                    isChecked = it
                    applyConfiguration(configuration, it, onChange = true)
                })
            }

            override fun onDetached() {
                super.onDetached()
                subscription.clear()
            }
        }
    }

fun <T : PreferenceGroup> T.action(action: () -> Unit, configurator: Configurator<Preference>? = null): Preference = addPref(configurator) {
    Preference(it).apply {
        setOnPreferenceClickListener {
            action()
            true
        }
    }
}

val <T: PreferenceGroup> T.list
    get() = AccessorPreferenceDsl<ListPreference, String>(this) { accessor, configuration ->
        object : ListPreference(this) {
            private val subscription = CompositeDisposable()


            override fun onBindViewHolder(holder: PreferenceViewHolder) {
                super.onBindViewHolder(holder)
                val outValue = TypedValue()
                context.theme.resolveAttribute(android.R.attr.selectableItemBackground, outValue, true)
                holder.itemView.setBackgroundResource(outValue.resourceId)
            }

            init {
                key = accessor.name
                onPreferenceChangeListener = accessor.value.update { (it as CharSequence).toString() }
                applyConfiguration(configuration, value, onChange = false)
            }

            override fun onAttached() {
                super.onAttached()
                subscription.add(accessor.value.bind(MainThreadScheduler) {
                    value = it
                    applyConfiguration(configuration, it, onChange = true)
                })
            }

            override fun onDetached() {
                super.onDetached()
                subscription.clear()
            }
        }
    }

val <T: PreferenceGroup> T.dropDown
    get() = AccessorPreferenceDsl<DropDownPreference, String>(this) { accessor, configuration ->
        object : DropDownPreference(this) {
            private val subscription = CompositeDisposable()

            init {
                key = accessor.name
                onPreferenceChangeListener = accessor.value.update { (it as CharSequence).toString() }
                applyConfiguration(configuration, value, onChange = false)
            }

            override fun onAttached() {
                super.onAttached()
                subscription.add(accessor.value.bind(MainThreadScheduler) {
                    value = it
                    applyConfiguration(configuration, it, onChange = true)
                })
            }

            override fun onDetached() {
                super.onDetached()
                subscription.clear()
            }
        }
    }

val <T : PreferenceGroup> T.textEditor
    get() = AccessorPreferenceDsl<EditTextPreference, String>(this) { accessor, configuration ->
        object : EditTextPreference(this) {
            private val subscription = CompositeDisposable()

            init {
                key = accessor.name
                onPreferenceChangeListener = accessor.value.update { (it as CharSequence).toString() }
                applyConfiguration(configuration, text, onChange = false)
            }

            override fun onAttached() {
                super.onAttached()
                subscription.add(accessor.value.bind(MainThreadScheduler) {
                    text = it
                    applyConfiguration(configuration, it, onChange = true)
                })
            }

            override fun onDetached() {
                super.onDetached()
                subscription.clear()
            }
        }
    }

class AccessorPreferenceDsl<T: Preference, V> internal constructor(
        private val parent: PreferenceGroup,
        private val impl: Context.(Setting<V>, Configuration<T, V>?) -> T
) {
    operator fun invoke(accessor: KMutableProperty0<V>, configurator: Configurator<T>? = null) =
            invoke(Setting(accessor), configurator)
    operator fun invoke(accessor: KMutableProperty0<V>, configurator: DynamicConfigurator<T, V>? = null) =
            invoke(Setting(accessor), configurator)

    operator fun invoke(accessor: Setting<V>, configurator: Configurator<T>? = null) =
            create(accessor, configuration = configurator?.let { Configuration.static(it) })

    operator fun invoke(accessor: Setting<V>, configurator: DynamicConfigurator<T, V>? = null) =
            create(accessor, configuration = configurator?.let { Configuration.dynamic(it) })

    private fun create(accessor: Setting<V>, configuration: Configuration<T, V>?): T = parent.addPref {
        impl(it, accessor, configuration)
    }
}

internal fun <T: Preference, V> T.applyConfiguration(configuration: Configuration<T, V>?, value: V, onChange: Boolean) {
    if (configuration != null && onChange == configuration.isDynamic) {
        configuration.apply(this, value)
    }
}

private fun <T : PreferenceGroup, P : Preference> T.addPref(configurator: Configurator<P>? = null, preferenceFactory: (Context) -> P): P {
    val preference = preferenceFactory(context)
    isPersistent = false
    addPreference(preference)
    configurator?.invoke(preference)
    return preference
}

private fun <T> RxVar<T>.update(mapping: (Any?) -> T) = Preference.OnPreferenceChangeListener { _, newValue ->
    value = mapping(newValue)
    true
}