package com.example.tracker.data.model

data class ExpenseCategory(
    val id: Long,
    val name: String
)

val expenseCategories = listOf(
    ExpenseCategory(1L, "카테고리1"),
    ExpenseCategory(2L, "카테고리2"),
    ExpenseCategory(3L, "카테고리3"),
    ExpenseCategory(4L, "카테고리4"),
    ExpenseCategory(5L, "카테고리5")
)