package com.example.tracker.data.dao

import android.content.ClipData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.tracker.data.entity.ItemDefinition
import kotlin.reflect.KCallable

@Dao
interface ItemDefinitionDao {
    @Insert
    suspend fun insert(item: ItemDefinition)

    @Update
    suspend fun update(item: ItemDefinition)

    @Delete
    suspend fun delete(item: ItemDefinition)

    @Query("SELECT * FROM item_definition")
    suspend fun getAll(): List<ItemDefinition>

    @Query("SELECT * FROM item_definition WHERE subCategoryId = :subCategoryId")
    suspend fun getBySubCategoryId(subCategoryId: Long): List<ItemDefinition>

    @Query("SELECT * FROM item_definition WHERE isActive = :isActive")
    suspend fun getActiveItems(isActive:Boolean): List<ItemDefinition>

    @Query("SELECT * FROM item_definition WHERE name LIKE '%' || :keyword || '%'")
    suspend fun getByName(keyword: String): List<ItemDefinition>

    @Query("SELECT name FROM item_definition WHERE id = :id")
    suspend fun getNameById(id: Long): String

    @Query("SELECT * FROM item_definition " +
            "WHERE subCategoryId = :subCategoryId AND name = :name AND store = :store AND kcalPerUnit = :kcalPerUnit AND defaultPrice = :defaultPrice AND id != :excludeId")
    suspend fun duplicationTest(subCategoryId: Long, name: String, store: String?, kcalPerUnit: Long?, defaultPrice: Long, excludeId: Long): List<ItemDefinition>

}