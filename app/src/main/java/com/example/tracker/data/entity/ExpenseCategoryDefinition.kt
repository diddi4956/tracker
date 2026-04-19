package com.example.tracker.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "expense_category_definition")
data class ExpenseCategoryDefinition(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val isDefault: Boolean = false, // 얘는 dao에 쿼리 넣을 필요가 없나?
    val isActive: Boolean = true
)
// 생활비, 간식외식비, 사치소비, 자기계발/교육비, 저축