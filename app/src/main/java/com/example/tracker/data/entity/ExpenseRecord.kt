package com.example.tracker.data.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "expense_record", indices = [Index(value = ["date", "itemId", "subCategoryId"], unique = true)])
data class ExpenseRecord(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val date: String,
    val subCategoryId: Long, // null봐주게되면 카테고리에도 속하지못하게됨.. 외래키걸기는 나중에 함 해보기\

    val itemId: Long,
    val unitPrice: Long,
    val quantity: Int,
    val memo: String = ""
    /* 데피니션은 리코드랑은 다르게 오래가는항목임. 날짜 초월한거. 그래서 사용하는지 안하는지를 체크해둔거. 저장만해두는지 지금 쓰는건지 여부.
     */
)
