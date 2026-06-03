package com.example.tracker.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "condition_spectrum_record")
data class ConditionSpectrumRecord (
    @PrimaryKey(autoGenerate = true)
    val id: Long,
    val definitionId: Long,
    val date: String,
    val spectrum: Int
)
