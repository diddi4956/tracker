package com.example.tracker.data.dto

data class ConditionTrackingByTagDto (
    val checkedCount: Int,
    val date: String,
    val conditionDefinitionId: Long,
    val definitionName: String,
    val tagName: String,
    val conditionCategoryId: Long
)