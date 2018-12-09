package com.github.dant3.androidexecutors

import android.os.Handler
import android.os.Looper
import java.util.concurrent.Executor

class LooperExecutor(private val looper: Looper) : Executor {
    private val handler = Handler(looper)

    private fun isLooperThread(): Boolean =
            looper.thread == Thread.currentThread()

    override fun execute(command: Runnable) {
        if (isLooperThread()) {
            command.run()
        } else {
            handler.post(command)
        }
    }

    fun executeLater(command: Runnable) {
        handler.post(command)
    }

    fun executeLater(command: () -> Unit) {
        handler.post(command)
    }

    fun executeAfter(delay: Long, command: () -> Unit) {
        handler.postDelayed(Runnable(command), delay)
    }
}
