package com.example.tracker.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.tracker.data.entity.ExpenseRecord

@Dao
interface ExpenseRecordDao {
    @Insert
    suspend fun insert(record: ExpenseRecord) // 여기서의 record는 그저 매개변수일뿐이니까 맘대로 적어도 전체 데이터베이스에 영향이 없겠구나

    @Update
    suspend fun update(record: ExpenseRecord)

    @Delete
    suspend fun delete(record: ExpenseRecord)

    @Query("SELECT * FROM expense_record")
    suspend fun getAll(): List<ExpenseRecord>

    @Query("SELECT * FROM expense_record WHERE categoryId = :categoryId")// 이거 뭔문법인지 모르겟다
    suspend fun getCategoryId(categoryId: Long): List<ExpenseRecord>

    @Query("SELECT * FROM expense_record WHERE subCategoryId = :subCategoryId")
    suspend fun getSubCategoryId(subCategoryId: Long): List<ExpenseRecord>

    @Query("SELECT * FROM expense_record WHERE date = :date")
    suspend fun getByDate(date: String): List<ExpenseRecord>
}
