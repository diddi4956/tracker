package com.example.tracker.data.dto

data class HabitTrackingByCategoryDto (
    val checkedCount: Int,
    val date: String,
    val definitionId: Long,
    val name: String,
    val categoryId: Long
)