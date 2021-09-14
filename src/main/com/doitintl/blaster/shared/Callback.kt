package com.doitintl.blaster.shared

import java.io.Closeable

interface Callback<T> : Closeable {
    fun call(t: T)
}