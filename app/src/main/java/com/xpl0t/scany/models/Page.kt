package com.xpl0t.scany.models

data class Page(
    val id: Int = 0,
    val image: ByteArray?,
    val next: Int?
)