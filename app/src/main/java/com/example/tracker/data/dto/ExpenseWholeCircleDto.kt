package com.example.tracker.data.dto

data class ExpenseWholeCircleDto (
    val categoryId: Long,
    val totalPrice: Long
)

data class ExpenseWholeCircle(
    val wholeCircle: ExpenseWholeCircleDto,
    val categoryName: String
)