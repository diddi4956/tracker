package com.example.tracker.data.dto

data class ExpenseCircleByCategoryDto(
    val subCategoryId: Long,
    val subCategoryName: String,
    val categoryId: Long,
    val totalPrice: Long
)