package com.example.tracker.data.dto

import androidx.room.Embedded
import com.example.tracker.data.entity.HabitRecord

data class DefinitionTracking(
    val definitionName: String,
    @Embedded
    val habitRecord: HabitRecord
)