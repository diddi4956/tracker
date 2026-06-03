package com.example.tracker.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "habit_category")
data class HabitCategoryDefinition (
    @PrimaryKey(autoGenerate = true)
    val id: Long,
    val name: String,
    val startDate: String?,
    val endDate:String?, // 그럼 만약에 date로 기간을 정해서 하는건데 date가 쿼리검색에 안되는 NULL인거면 모든 기간에 한정이란뜻으로 쓰고싶었던건데 쿼리검색에선 어케되려는지 모르겠음.
    // val period: String?,
    val isActive: Boolean
)