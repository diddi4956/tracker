package com.example.tracker.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "condition_record",
    foreignKeys = [ForeignKey(
        entity = ConditionDefinition::class,
        parentColumns = ["id"],
        childColumns = ["conditionDefinitionId"],
        onDelete = ForeignKey.CASCADE
        )],
            indices = [
                Index(value = ["date", "conditionDefinitionId"], unique = true),
                Index("conditionDefinitionId")
            ]
) // 2026/4/30 - 생리(tag) 같은 중복을 방지하기 위해 복합 주키 -> 유니크로 변경.. 유니크가 더 깔끔할듯
data class ConditionCheckRecord(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val date: String,
    val conditionDefinitionId: Long, // 외래키 입력하기
)