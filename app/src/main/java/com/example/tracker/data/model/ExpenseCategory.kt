package com.example.tracker.data.model

data class IdWithName(
    val id: Long,
    val name: String
)
val expenseCategories = listOf(
    IdWithName(1L, "카테고리1"),
    IdWithName(2L, "카테고리2"),
    IdWithName(3L, "카테고리3"),
    IdWithName(4L, "카테고리4"),
    IdWithName(5L, "카테고리5")
)