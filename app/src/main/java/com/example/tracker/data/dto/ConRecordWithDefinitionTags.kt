package com.example.tracker.data.dto

import androidx.room.Embedded
import com.example.tracker.data.entity.ConditionTag

data class ConRecordWithDefinitionTags (
    val recordId: Long,
    @Embedded
    val tag: ConditionTag
)