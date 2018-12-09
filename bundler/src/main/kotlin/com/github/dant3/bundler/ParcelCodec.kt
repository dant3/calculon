package com.github.dant3.bundler

import android.os.Bundle
import android.os.Parcel

interface ParcelCodec<T> {
    fun write(parcel: Parcel, value: T, flags: Int)
    fun read(parcel: Parcel): T

    companion object {
        fun <T> fromBundleCodec(bundleCodec: BundleCodec<T>): ParcelCodec<T> = object: ParcelCodec<T> {
            override fun write(parcel: Parcel, value: T, flags: Int) {
                val bundle = Bundle()
                bundleCodec.write(bundle, value)
                parcel.writeBundle(bundle)
            }

            override fun read(parcel: Parcel): T {
                val bundle = parcel.readBundle(ClassLoader.getSystemClassLoader())
                return bundleCodec.read(bundle)
            }
        }
    }
}