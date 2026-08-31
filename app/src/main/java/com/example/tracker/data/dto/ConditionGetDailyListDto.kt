package com.example.tracker.data.dto

data class ConditionGetDailyListDto (
    val id: Long,
    val tagId: Long,
    val name: String, // conditionName
    val tagName: String, // UI에 띄워주기 위해 필요
    val checked: Boolean
)