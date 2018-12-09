package com.github.dant3.bundler

import android.os.Bundle
import kotlin.reflect.KClass
import kotlin.reflect.KParameter
import kotlin.reflect.KProperty1
import kotlin.reflect.full.isSubclassOf
import kotlin.reflect.full.memberProperties
import kotlin.reflect.full.primaryConstructor
import kotlin.reflect.jvm.jvmErasure

class DataClassBundleCodec<T: Any>(private val targetClass: KClass<T>): BundleCodec<T> {
    override fun write(bundle: Bundle, value: T) {
        for (kproperty in targetClass.memberProperties) {
            bundle.putProperty(value, kproperty)
        }
    }

    override fun read(bundle: Bundle): T {
        return bundle.getDataObject(targetClass)
    }

    fun <T: Any> Bundle.putDataObject(dataObject: T) {
        @Suppress("UNCHECKED_CAST")
        val kclass: KClass<T> = dataObject::class as KClass<T>
        for (property in kclass.memberProperties) {
            putProperty(dataObject, property)
        }
    }

    private fun <T> Bundle.putProperty(instance: T, kproperty: KProperty1<T, *>) {
        val fieldName = kproperty.name
        val value = kproperty.get(instance)
        when {
            value is Int -> putInt(fieldName, value)
            value is Long -> putLong(fieldName, value)
            value is String -> putString(fieldName, value)
            value == null -> {} // no actions required
            value::class.isData ->
                putBundle(fieldName, Bundle().apply {
                    putDataObject(value)
                })
            else -> TODO("Can't handle type ${value.javaClass.name} - unknown")
        }
    }

    fun <T: Any> Bundle.getDataObject(kclass: KClass<T>): T {
        val constructor = kclass.primaryConstructor!!
        return constructor.callBy(constructor.parameters.associateBy({ it }) {
            getParameter(it)
        })
    }

    private fun Bundle.getParameter(parameter: KParameter): Any? {
        val name = parameter.name
        val type = parameter.type
        val kclass = type.jvmErasure
        return when {
            type.isMarkedNullable && !containsKey(name) -> null
            kclass.isSubclassOf(String::class) -> getString(name)
            kclass.isSubclassOf(Int::class) -> getInt(name)
            kclass.isSubclassOf(Long::class) -> getLong(name)
            kclass.isData -> getBundle(name).getDataObject(kclass)
            else -> TODO("Can't handle type $kclass - unknown")
        }
    }

    companion object {
        inline operator fun <reified T: Any> invoke(): BundleCodec<T> {
            val klass = T::class
            require(klass.isData) {
                "Only data classes are supported"
            }
            return DataClassBundleCodec(klass)
        }
    }
}