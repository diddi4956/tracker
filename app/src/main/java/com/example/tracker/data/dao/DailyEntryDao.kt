package com.example.tracker.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.tracker.data.entity.DailyEntry

@Dao
interface DailyEntryDao {

    @Insert
    suspend fun insert(entry: DailyEntry)

    @Update
    suspend fun update(entry: DailyEntry)

    @Delete
    suspend fun delete(entry: DailyEntry)

    @Query("SELECT * FROM daily_entry")
    suspend fun getAll(): List<DailyEntry>

    @Query("SELECT * FROM daily_entry WHERE date = :date")
    suspend fun getByDate(date: String): List<DailyEntry>
}