package com.example.tracker.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.tracker.data.entity.HabitDefinition

@Dao
interface HabitDefinitionDao {

    @Insert
    suspend fun insert(habit: HabitDefinition)

    @Update
    suspend fun update(habit: HabitDefinition)

    @Delete
    suspend fun delete(habit: HabitDefinition)

    @Query("SELECT * FROM habit_definition")
    suspend fun getAll(): List<HabitDefinition>

    @Query("SELECT * FROM habit_definition WHERE isActive = :isActive")
    suspend fun getByIsActive(isActive: Boolean): List<HabitDefinition>

    @Query("SELECT * FROM habit_definition WHERE id = :habitDefinitionId")
    suspend fun findDefinition(habitDefinitionId: Long): HabitDefinition?

    @Transaction
    suspend fun addHabit(def: HabitDefinition){
        val existing = findDefinition(def.id)
        if(existing == null) {
            insert(def)
        }else{
            // 이미 있다고 알림보내기
        }
    }
}