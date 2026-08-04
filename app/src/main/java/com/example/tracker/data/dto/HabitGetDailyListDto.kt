package com.example.tracker.data.dto

data class HabitGetDailyListDto (
    val id: Long,
    val categoryId: Long,
    val name:String,
    val categoryName: String,
    val recordId: Long?,
    val checked: Boolean
)