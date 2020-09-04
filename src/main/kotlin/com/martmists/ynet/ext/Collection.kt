package com.martmists.ynet.ext

fun <T> Collection<T>.next(current: T): T {
    val next = indexOf(current) + 1
    if (next == size) {
        return this.elementAt(0)
    }
    return this.elementAt(next)
}
