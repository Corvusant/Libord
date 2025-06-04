package com.atomic_crucible.libord

data class Category (
    val value: String
)

data class Word(
    val value: String,
    val categories: List<Category>
)

