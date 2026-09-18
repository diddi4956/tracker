package com.example.tracker.data.dto

import androidx.room.Embedded
import com.example.tracker.data.entity.ConditionCheckRecord

data class ConditionCheckRecordAndDefinitionName (
    @Embedded
    val conditionCheckedRecord: ConditionCheckRecord,
    val definitionName: String
)