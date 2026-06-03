package com.example.tracker.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "condition_category")
data class ConditionCategory (
    @PrimaryKey(autoGenerate = true)
    val id: Long,
    val name: String
)