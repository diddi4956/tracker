package com.example.tracker.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "daily_entry")
data class DailyEntry(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val date: String,
    val memo: String = ""
) //이걸로 끝내도 되나 뭔가 더 추가할건 없을까 그건 나중에 생각하면 될듯?ㅋㅋ