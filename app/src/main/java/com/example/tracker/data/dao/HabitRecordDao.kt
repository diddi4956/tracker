package com.example.tracker.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.tracker.data.entity.HabitRecord

@Dao
interface HabitRecordDao {
    @Insert
    suspend fun insert(record: HabitRecord)

    @Update
    suspend fun update(record: HabitRecord)

    @Delete
    suspend fun delete(record: HabitRecord)

    @Query("SELECT * FROM habit_record")
    suspend fun getAll(): List<HabitRecord>

    @Query("SELECT * FROM habit_record WHERE habitId =:habitId")
    suspend fun getByHabitId(habitId: Long): List<HabitRecord>

    @Query("SELECT * FROM habit_record WHERE date =:date")
    suspend fun getByDate(date: String): List<HabitRecord>

    @Query("SELECT * FROM habit_record WHERE checked = :checked")
    suspend fun getByChecked(checked: Boolean) : List<HabitRecord>
}