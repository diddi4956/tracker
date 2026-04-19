package com.example.tracker.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "expense_record")
data class ExpenseRecord(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val date: String,
    val categoryId: Long, //
    val subCategoryId: Long?, //
    val itemId: Long?,
    val amount: Int,
    val quantity: Float? = null,
    val memo: String = "" //여기는 isActive라던가 그런게 없네? 왜지? 무슨차이?
    /* 데피니션은 리코드랑은 다르게 오래가는항목임. 날짜 초월한거. 그래서 사용하는지 안하는지를 체크해둔거. 저장만해두는지 지금 쓰는건지 여부.
     */
)
