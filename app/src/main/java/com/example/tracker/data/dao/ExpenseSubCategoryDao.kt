package com.example.tracker.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.tracker.data.entity.ExpenseSubCategoryDefinition

@Dao
interface ExpenseSubCategoryDao {

    @Insert
    suspend fun insert(subCategory: ExpenseSubCategoryDefinition)

    @Update
    suspend fun update(subCategoryDao: ExpenseSubCategoryDefinition)

    @Delete
    suspend fun delete(subCategoryDao: ExpenseSubCategoryDefinition)

    @Query("SELECT * FROM expense_subcategory_definition")
    suspend fun getAll(): List<ExpenseSubCategoryDefinition>

    @Query("SELECT * FROM expense_subcategory_definition WHERE categoryId = :categoryId")
    suspend fun getByCategoryId(categoryId: Long): List<ExpenseSubCategoryDefinition> //여기에 있는 매개변수인 categoryId값이 :categoryId자리에 들어가는거임.

    @Query("SELECT * FROM expense_subcategory_definition WHERE isActive = :isActive")
    suspend fun getActiveSubCategories(isActive:Boolean): List<ExpenseSubCategoryDefinition>

    @Query("SELECT name FROM expense_subcategory_definition WHERE id = :id")
    suspend fun getNameById(id: Long):String

}