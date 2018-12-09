package com.github.dant3.bundler

import android.os.Bundle
import kotlin.reflect.KClass
import kotlin.reflect.KParameter
import kotlin.reflect.KProperty1
import kotlin.reflect.full.isSubclassOf
import kotlin.reflect.full.memberProperties
import kotlin.reflect.full.primaryConstructor
import kotlin.reflect.jvm.jvmErasure

interface BundleCodec<T> {
    fun write(bundle: Bundle, value: T)
    fun read(bundle: Bundle): T

    companion object {
        fun <T> BundleCodec<T>.createBundle(value: T) = Bundle().also { write(it, value) }
        fun <T> createBundle(value: T, codec: BundleCodec<T>) = Bundle().also { codec.write(it, value) }
        fun <T> Bundle.read(codec: BundleCodec<T>) = codec.read(this)

        fun <T> forList(itemCodec: BundleCodec<T>) = object : BundleCodec<List<T>> {
            override fun write(bundle: Bundle, value: List<T>) {
                bundle.putInt("list_size", value.size)
                value.forEachIndexed { index, item ->
                    bundle.putBundle("item_$index", createBundle(item, itemCodec))
                }
            }

            override fun read(bundle: Bundle): List<T> {
                val listSize = bundle.getInt("list_size")
                return (0 until listSize).map { index ->
                    itemCodec.read(bundle.getBundle("item_$index"))
                }
            }
        }

        inline fun <reified T : Any> forDataClass(): BundleCodec<T> {
            require(T::class.isData)

            return object : BundleCodec<T> {
                override fun write(bundle: Bundle, value: T) {
                    val kclass = T::class
                    for (kproperty in kclass.memberProperties) {
                        bundle.putProperty(value, kproperty)
                    }
                }

                override fun read(bundle: Bundle): T {
                    return bundle.getDataObject(T::class)
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
            }
        }
    }
}