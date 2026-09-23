package com.example.tracker.data.dto

import androidx.room.Embedded
import com.example.tracker.data.entity.ConditionCheckRecord
import com.example.tracker.data.entity.ConditionTag

data class ConditionRecordWithTags(
    val conditionRecord: ConditionCheckRecordAndDefinitionName,
    val tags: List<ConditionTag>
)