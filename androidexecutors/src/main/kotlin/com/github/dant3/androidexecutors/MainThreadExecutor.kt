package com.github.dant3.androidexecutors

import android.os.Looper

val MainThreadExecutor = LooperExecutor(Looper.getMainLooper())