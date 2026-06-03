package com.example.tracker.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "condition_tag")
data class ConditionTag (
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String
)