package com.github.dant3.rxvar


import io.reactivex.Observable
import io.reactivex.Observer
import io.reactivex.Scheduler
import io.reactivex.Single
import io.reactivex.disposables.Disposable
import io.reactivex.functions.Consumer
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject

abstract class RxVal<T> : Observable<T>() {
    abstract val value: T

    fun bind(consumer: Consumer<T>): Disposable {
        consumer.accept(value)
        return subscribe(consumer)
    }

    fun bind(scheduler: Scheduler = Schedulers.from { it.run() }, consumer: Consumer<T>): Disposable {
        return bind(scheduler) {
            consumer.accept(it)
        }
    }

    fun bind(scheduler: Scheduler = Schedulers.from { it.run() }, consumer: (T) -> Unit): Disposable {
        consumer(value)
        return observeOn(scheduler).subscribe(consumer)
    }

    fun singleUpdate(): Observable<T> = take(1)
    fun toSingle(): Single<T> = singleUpdate().singleOrError()

    companion object {
        fun <T> memoizeOf(initialValue: T, observable: Observable<out T>): RxVal<T> = object: RxVal<T>() {
            private var lastValue: T = initialValue

            private val subject = PublishSubject.create<T>()
            private val subjectProxyObserver = object: Observer<T> {
                override fun onNext(t: T) {
                    lastValue = t
                    subject.onNext(t)
                }

                override fun onComplete() {
                    subject.onComplete()
                }

                override fun onError(e: Throwable) {
                    subject.onError(e)
                }

                override fun onSubscribe(d: Disposable) {
                    subject.onSubscribe(d)
                }
            }

            init {
                observable.subscribe(subjectProxyObserver)
            }

            override val value: T
                get() = lastValue

            override fun subscribeActual(observer: Observer<in T>) {
                subject.subscribe(observer)
            }
        }
    }
}


fun <I, O> RxVal<I>.mapValue(mapping: (I) -> O): RxVal<O> = object: RxVal<O>() {
    override val value: O
        get() = mapping(this@mapValue.value)

    override fun subscribeActual(observer: Observer<in O>) {
        observer.onSubscribe(this@mapValue.subscribe {
            observer.onNext(mapping(it))
        })
    }
}