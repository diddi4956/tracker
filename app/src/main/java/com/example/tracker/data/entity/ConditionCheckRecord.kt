package com.example.tracker.data.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "condition_record", indices = [Index(value = ["date", "conditionDefinitionId"], unique = true)]
) // 2026/4/30 - 생리(tag) 같은 중복을 방지하기 위해 복합 주키 -> 유니크로 변경.. 유니크가 더 깔끔할듯
data class ConditionCheckRecord(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val date: String,
    val conditionDefinitionId: Long, // conditionDefinition과 연결
    // val checked: Boolean// 이거 기본이 true? 일단 dao엔 false가 기본
)