package com.example.tracker.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.tracker.data.entity.ConditionRecord

@Dao
interface ConditionRecordDao {
    @Insert
    suspend fun insert(record: ConditionRecord)

    @Update
    suspend fun update(record: ConditionRecord)

    @Delete
    suspend fun delete(record: ConditionRecord)

    @Query("SELECT * FROM condition_record")
    suspend fun getAll(): List<ConditionRecord>

    @Query("SELECT * FROM condition_record WHERE conditionId = :conditionId")
    suspend fun getByConditionId(conditionId: Long): List<ConditionRecord>

    @Query("SELECT * FROM condition_record WHERE date = :date")
    suspend fun getByDate(date: String): List<ConditionRecord>
}