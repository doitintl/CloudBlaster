package com.doitintl.blaster.lister

import java.io.Closeable

interface Callback<T> : Closeable {
    fun call(s: T)
}