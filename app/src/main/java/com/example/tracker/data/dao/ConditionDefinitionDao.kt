package com.example.tracker.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.tracker.data.entity.ConditionDefinition

@Dao
interface ConditionDefinitionDao {
    @Insert
    suspend fun insert(condition: ConditionDefinition)

    @Update
    suspend fun update(condition: ConditionDefinition)

    @Delete
    suspend fun delete(condition: ConditionDefinition)

    @Query("SELECT * FROM condition_definition")
    suspend fun getAll(): List<ConditionDefinition>

    @Query("SELECT * FROM condition_definition WHERE isActive = 1")
    suspend fun getActiveCondition(): List<ConditionDefinition>
}