package com.github.dant3.rxdroid

import com.github.dant3.androidexecutors.MainThreadExecutor
import io.reactivex.schedulers.Schedulers

val MainThreadScheduler = Schedulers.from(MainThreadExecutor)