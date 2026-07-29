package com.example.tracker.data.dto

data class ConditionGetDailyListDto (
    val id: Long,
    val tagId: Long,
    val name: String,
    // val tagName: String,
    val checked: Boolean
)