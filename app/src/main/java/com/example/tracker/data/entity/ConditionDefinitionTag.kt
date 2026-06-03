package com.example.tracker.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "condition_definition_tag")

data class ConditionDefinitionTag(

    @PrimaryKey(autoGenerate = true)

    val id: Long = 0,

    val conditionDefinitionId: Long,

    val tagId: Long

) // 중간엔티티(관계저장) -> 다대다로 만들 수 있음