package com.example.tracker.data.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "habit_record", indices = [Index(value = ["date", "habitDefinitionId"], unique = true)])
data class HabitRecord(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val date: String, //
    val habitDefinitionId: Long, //
    val checked: Boolean
)
