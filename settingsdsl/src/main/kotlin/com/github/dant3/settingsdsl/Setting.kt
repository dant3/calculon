package com.github.dant3.settingsdsl


import com.github.dant3.rxvar.RxVar
import com.github.dant3.rxvar.map
import kotlin.reflect.KMutableProperty0

interface Setting<T> {
    val name: String
    val value: RxVar<T>

    companion object {
        operator fun <T> invoke(property: KMutableProperty0<T>) = object : Setting<T> {
            override val name = property.name
            override val value: RxVar<T> = RxVar(property.get()).apply {
                subscribe { property.set(it) }
            }
        }
    }

    fun read() = value.value
    fun write(value: T) {
        this.value.value = value
    }
}

fun <I, O> Setting<I>.map(convert: (I) -> O, reverseConvert: (O) -> I): Setting<O> = object : Setting<O> {
    override val name: String = this@map.name
    override val value: RxVar<O> = this@map.value.map(convert, reverseConvert)
}

fun <T : Any> Setting<T?>.withDefaultValue(defaultValue: T): Setting<T> = map(
        convert = { it ?: defaultValue },
        reverseConvert = { it }
)