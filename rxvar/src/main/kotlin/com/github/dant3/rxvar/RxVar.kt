package com.github.dant3.rxvar


import io.reactivex.Observer
import io.reactivex.internal.disposables.CancellableDisposable
import java.util.concurrent.CopyOnWriteArraySet

abstract class RxVar<T> protected constructor(): RxVal<T>() {
    abstract override var value: T

    companion object {
        operator fun <T> invoke(initialValue: T): RxVar<T> = object : RxVar<T>() {
            private val observers: MutableSet<Observer<in T>> = CopyOnWriteArraySet()
            private var currentValue: T = initialValue

            override var value: T
                get() = currentValue
                set(value) {
                    currentValue = value
                    notifyObservers(value)
                }

            override fun subscribeActual(observer: Observer<in T>) {
                val subscription = CancellableDisposable {
                    observers.remove(observer)
                }
                observers.add(observer)
                observer.onSubscribe(subscription)
            }

            protected fun notifyObservers(value: T) {
                val currentObserversSet = observers.toSet()
                currentObserversSet.forEach { it.onNext(value) }
            }
        }

    }
}

fun <I, O> RxVar<I>.map(mapping: (I) -> O, reverseMapping: (O) -> I): RxVar<O> = object: RxVar<O>() {
    override var value: O
        get() = mapping(this@map.value)
        set(value) {
            this@map.value = reverseMapping(value)
        }

    override fun subscribeActual(observer: Observer<in O>) {
        observer.onSubscribe(this@map.subscribe { value: I ->
            observer.onNext(mapping(value))
        })
    }
}