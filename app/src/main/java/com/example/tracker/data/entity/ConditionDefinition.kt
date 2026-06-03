package com.example.tracker.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "condition_definition")
data class ConditionDefinition(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val conditionCategoryId: Long,
    // val type: Int, // 이거 bool로 하면 안되나 -> 일단 스펙트럼은...생략해봄 굳이 기록할때 과한 선택지같기도함 안쓸듯..
    val isActive: Boolean = true,
    val frequency: Int = 0
)