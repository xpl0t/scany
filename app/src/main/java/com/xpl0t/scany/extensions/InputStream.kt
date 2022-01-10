package com.xpl0t.scany.extensions

import java.io.InputStream
import java.util.regex.Pattern

fun InputStream.readTable(): Sequence<List<String>> {
    val spacePattern = Pattern.compile("\\s+")
    val lines = bufferedReader().use { it.readText() }.lineSequence()

    return lines.map { it.split(spacePattern) }
}
