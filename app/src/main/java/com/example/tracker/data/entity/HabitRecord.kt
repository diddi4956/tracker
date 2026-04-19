package com.example.tracker.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "habit_record")
data class HabitRecord(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val date: String, //
    val habitId: Long, //
    val checked: Boolean //
)
