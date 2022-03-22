package com.xpl0t.scany.models

data class Scan(
    val id: Int = 0,
    val name: String,
    val pages: List<Page> = listOf()
)