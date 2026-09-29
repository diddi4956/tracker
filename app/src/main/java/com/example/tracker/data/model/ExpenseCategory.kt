package com.example.tracker.data.model

data class IdWithName(
    val id: Long,
    val name: String
)
val expenseCategories = listOf(
    IdWithName(1L, "생활비"),
    IdWithName(2L, "간식/외식비"),
    IdWithName(3L, "비계획소비"),
    IdWithName(4L, "자기계발/교육비"),
    IdWithName(5L, "취미비용")
)