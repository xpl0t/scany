package com.xpl0t.scany.util

class Optional<T>(private val optional: T?) {

    val value: T get() = optional!!
    val isEmpty: Boolean get() = optional == null

    companion object {
        fun <T> empty(): Optional<T> {
            return Optional(null)
        }
    }
}