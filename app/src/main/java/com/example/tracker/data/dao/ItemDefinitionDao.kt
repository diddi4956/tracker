package com.example.tracker.data.dao

import android.content.ClipData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.tracker.data.entity.ItemDefinition

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

    @Transaction
    suspend fun insertOrGetCandidates(item: ItemDefinition): List<ItemDefinition>{
        val existings= getByName(item.name)
        val sames = mutableListOf<ItemDefinition>()
        for(e in existings){
            if(e.subCategoryId == item.subCategoryId && e.name == item.name && e.store == item.store && e.kcalPerUnit == item.kcalPerUnit && e.defaultPrice == item.defaultPrice){
                sames.add(e)
            }
        }
        if(sames.isEmpty()) {
            insert(item)
        }
        return sames
    }

    @Transaction // candidate = 같은거. 있으면 동작하면 안됨.
    suspend fun updateOrGetCandidates(item: ItemDefinition): List<ItemDefinition>{
        val existings= getByName(item.name)
        val sames = mutableListOf<ItemDefinition>()
        for(e in existings){
            if(e.subCategoryId == item.subCategoryId && e.name == item.name && e.store == item.store && e.kcalPerUnit == item.kcalPerUnit && e.defaultPrice == item.defaultPrice && e.id != item.id){
                sames.add(e)
            }
        }
        if(sames.isEmpty()) {
            update(item)
        }
        return sames
    }

}