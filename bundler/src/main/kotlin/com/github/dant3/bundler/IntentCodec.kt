package com.github.dant3.bundler

import android.content.Intent
import android.os.Bundle

interface IntentCodec<R> {
    fun read(intent: Intent): R
    fun write(data: R): Intent = write(Intent(), data)
    fun write(intent: Intent, data: R): Intent

    companion object {
        fun <T> fromBundleCodec(bundleCodec: BundleCodec<T>): IntentCodec<T> = object: IntentCodec<T> {
            override fun read(intent: Intent): T = bundleCodec.read(intent.extras)
            override fun write(intent: Intent, data: T): Intent = intent.apply {
                intent.putExtras(Bundle().apply { bundleCodec.write(this, data) })
            }
        }
    }
}