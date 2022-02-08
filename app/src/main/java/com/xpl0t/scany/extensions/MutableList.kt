package com.xpl0t.scany.extensions


/**
 * Execute the lambda expression and adds the result to the list.
 */
fun <T> MutableList<T>.add(generateValue: () -> T) {
    add(generateValue())
}
