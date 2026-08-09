package com.example.tracker.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.tracker.data.entity.HabitCategoryDefinition

@Dao
interface HabitCategoryDefinitionDao {
    @Insert
    suspend fun insert(category: HabitCategoryDefinition)

    @Update
    suspend fun update(category: HabitCategoryDefinition)

    @Delete
    suspend fun delete(category: HabitCategoryDefinition)

    @Query("SELECT * FROM habit_category")
    suspend fun getAll(): List<HabitCategoryDefinition>

    @Query("SELECT * FROM habit_category WHERE id = :habitCategoryId")
    suspend fun findHabitProject(habitCategoryId: Long): HabitCategoryDefinition?

}