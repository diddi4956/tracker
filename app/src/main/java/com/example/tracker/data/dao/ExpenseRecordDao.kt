package com.example.tracker.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.tracker.data.dto.ExpenseCircleByCategoryDto
import com.example.tracker.data.dto.ExpenseDailyPriceDto
import com.example.tracker.data.dto.ExpenseTrackingDto
import com.example.tracker.data.dto.ExpenseWholeCircleDto
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


    @Query("SELECT * FROM expense_record WHERE subCategoryId = :subCategoryId")
    suspend fun getSubCategoryId(subCategoryId: Long): List<ExpenseRecord>

    @Query("SELECT * FROM expense_record WHERE date = :date")
    suspend fun getByDate(date: String): List<ExpenseRecord>

    // 원하는 기간에 따라 날짜, 서브카테고리를 가져옴. 아이템이 아닌 서브카테고리별로 체크(ㅇㅇ샴푸 등이 아닌 헤어오일, 헤어세척비누?뭐 이런식)
    @Query("SELECT date, subCategoryId FROM expense_record WHERE date BETWEEN :start AND :end")
    suspend fun tracking(start: String, end: String): List<ExpenseTrackingDto>

    @Query("SELECT s.id AS subCategoryId, s.name AS subCategoryName, s.categoryId  AS categoryId, SUM(r.unitPrice*r.quantity) AS totalPrice FROM expense_record r INNER JOIN expense_subcategory_definition s ON r.subCategoryId = s.id WHERE date BETWEEN :start AND :end AND s.categoryId =:categoryId GROUP BY s.id, s.name ORDER BY totalPrice DESC")
    // s.name이랑 s.id가 group by에 들어가는 이유는 캡슐화? 뭐 그런거임...s.name에 유니크 걸어서 중복 안되게 하면 s.id로 하나 s.name으로 하나 전부 주 키로 쓸 수 있지만 캡슐화를 위한거임...캡슐화가 아닌가
    suspend fun circleGraphingByCategory(start: String, end: String, categoryId: Long): List<ExpenseCircleByCategoryDto>

    @Query("SELECT s.id AS subCategoryId, SUM(r.unitPrice * r.quantity) AS totalPrice FROM expense_subcategory_definition AS s INNER JOIN expense_record AS r ON r.subCategoryId = s.id WHERE r.date BETWEEN :start AND :end GROUP BY s.categoryId ORDER BY totalPrice DESC") // group by = sum의 계산 단위
    suspend fun wholeCircleGraphing(start: String, end: String) : List<ExpenseWholeCircleDto>

    @Query("SELECT date, SUM(quantity*unitPrice) AS dailyTotalPrice FROM expense_record WHERE date BETWEEN :start AND :end GROUP BY date ORDER BY date ASC")
    suspend fun calcDailyExpense(start:String, end: String): List<ExpenseDailyPriceDto>

    // 트랜잭션 추가
    // 같은 date + itemId +subCategoryId 기록이 있는지 조회
    // 있으면 quantity 증가해서 update
    // 없으면 insert

    @Query("SELECT * FROM expense_record WHERE date = :date AND itemId = :itemId AND subCategoryId = :subCategoryId LIMIT 1")
    suspend fun findSameExpenseRecord(date: String, itemId: Long, subCategoryId: Long): ExpenseRecord?

    @Transaction
    suspend fun addExpenseRecord(record: ExpenseRecord){
        val existing = findSameExpenseRecord(record.date, record.itemId, record.subCategoryId)

        if(existing == null){
            insert(record)
        } else{
            update(existing.copy(quantity = existing.quantity + record.quantity)) // data class 기능, 특정변수만 집어서 변경가능
        }
    }
}
