package com.example.tracker.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.tracker.data.entity.ExpenseCategoryDefinition

@Dao
interface ExpenseCategoryDefinitionDao {
    @Insert
    suspend fun insert(expense: ExpenseCategoryDefinition)

    @Update
    suspend fun update(expense: ExpenseCategoryDefinition)

    @Delete
    suspend fun delete(expense: ExpenseCategoryDefinition)

    @Query("SELECT * FROM expense_category_definition")
    suspend fun getAll(): List<ExpenseCategoryDefinition>

    @Query("SELECT * FROM expense_category_definition WHERE isActive = :isActive")
    suspend fun getActive(isActive: Boolean): List<ExpenseCategoryDefinition>
}