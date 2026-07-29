package com.example.tracker.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.tracker.data.dto.HabitGetCategoryListDto
import com.example.tracker.data.dto.HabitGetDailyListDto
import com.example.tracker.data.dto.HabitGetDefinitionListDto
import com.example.tracker.data.dto.HabitGetMonthlyByCategoryDto
import com.example.tracker.data.dto.HabitTrackingByCategoryDto
import com.example.tracker.data.dto.HabitTrackingByDefinitionDto
import com.example.tracker.data.entity.HabitRecord

@Dao
interface HabitRecordDao {
    @Insert
    suspend fun insert(record: HabitRecord)

    @Update
    suspend fun update(record: HabitRecord)

    @Delete
    suspend fun delete(record: HabitRecord)

    @Query("SELECT * FROM habit_record")
    suspend fun getAll(): List<HabitRecord>

    @Query("SELECT * FROM habit_record WHERE id =:id")
    suspend fun getById(id: Long): List<HabitRecord>

    @Query("SELECT * FROM habit_record WHERE date =:date")
    suspend fun getByDate(date: String): List<HabitRecord>// 원하는 날짜에 한해서만 하는거니까 리스트가 필요없나?

    // 데피니션은 기간을 알기 위해 카테고리와 조인해야함, 리코드는 그럴 필요 없음
    // 트래킹페이지 -1
    @Query("SELECT date, habitDefinitionId " +
            "FROM habit_record " +
            "WHERE date BETWEEN :start AND :end") // start, end는 사용자가 입력하는 통계기간
    suspend fun trackingByDefinition(start: String, end: String): List<HabitTrackingByDefinitionDto>

    @Query("SELECT d.name AS habitDefinitionName, d.id AS definitionId " +
            "FROM habit_definition AS d INNER JOIN habit_category AS c ON d.categoryId = c.id " +
            "WHERE (c.startDate IS NULL OR c.startDate <= :end) AND (c.endDate IS NULL OR c.endDate >= :start)")
    suspend fun getDefinitionList(start: String, end: String): List<HabitGetDefinitionListDto>
    // 가로(날짜) 세로(해빗데피니션 아이디)로 맞춤하여 보여줌

    // 트래킹페이지 -2 // 카운트를 하는 이유는 카운트된 횟수가 많으면 점(?)의 색이 그만큼 진해짐
    @Query("SELECT COUNT(r.id) AS checkedCount, r.date AS date, r.habitDefinitionId AS definitionId, d.name AS name, d.categoryId AS categoryId " +
            "FROM habit_definition AS d INNER JOIN habit_record AS r ON d.id = r.habitDefinitionId " +
            "WHERE r.date BETWEEN :start AND :end " +
            "GROUP BY r.date, d.categoryId") // 혹시 이거 그룹하는 순서도 중요한건가? 여튼. 카테고리별로 날짜마다 count해야함
    suspend fun trackingByCategory(start: String, end: String): List<HabitTrackingByCategoryDto>

    @Query("SELECT name, id " +
            "FROM habit_category " +
            "WHERE (startDate IS NULL OR startDate <= :end) AND (endDate IS NULL OR endDate >= :start)")
    suspend fun getCategoryList(start: String, end: String): List<HabitGetCategoryListDto>
    // 가로(날짜) 세로(카테고리 아이디)로 맞춤하여 ui보여줌

    // 먼슬리로 추이 보기
    @Query("SELECT r.date AS date, COUNT(r.id) AS countOfRecord " +
            "FROM habit_record AS r INNER JOIN habit_definition AS d ON r.habitDefinitionId = d.id " +
            "WHERE d.categoryId = :categoryId AND r.date BETWEEN :start AND :end GROUP BY r.date")
    suspend fun getMonthlyByCategory(categoryId: Long, start: String, end: String): List<HabitGetMonthlyByCategoryDto>

    // 데일리 체크리스트 -> 이걸 이렇게 해서 데일리 리스트를 만들어주고(만들때 프로젝트명(카테고리명)이나 우선순위가 필요한데 전부 데피니션에 있음...) 그 후에 리코드를 조작하는 식으로 해야하나 하나 참...어렵네
    @Query("SELECT d.id AS id, d.categoryId AS categoryId, c.name AS categoryName, r.id AS recordId, CASE WHEN r.id IS NULL THEN 0 ELSE 1 END AS checked " +
            "FROM habit_definition d INNER JOIN habit_category AS c ON d.categoryId = c.id LEFT JOIN habit_record r ON d.id = r.habitDefinitionId AND r.date = :date " +
            "WHERE (c.startDate IS NULL OR c.startDate <= :date) AND (c.endDate IS NULL OR c.endDate >= :date) ORDER BY importance") // 리코드 없는 데피니션이 사라지면 안되기에 레프트조인
    suspend fun getDailyList(date:String): List<HabitGetDailyListDto>
    // startDate<= :date <=endDate 이거를 쿼리로 어케 쓰지? WHERE에 추가하고싶은데.
    // 데일리 체크리스트는 dto를 두개 써야하나? -> 보여주기용 쿼리와 체크 변경용 쿼리는 다름. 보여주기쿼리는 상태변경(리코드 디비 변경)에 따라 ui가 리셋됨
    // 상태객체 : 내가 만든 dto/데이터를 컴포즈 상태 객체에 담는다 즉 상태 객체 자체는 컴포즈등에서 제공, 그 안에 들어가는 데이터는 내가 만든 dto


    @Query("SELECT * FROM habit_record WHERE date = :date AND habitDefinitionId = :habitDefinitionId")
    suspend fun findRecord(date: String, habitDefinitionId: Long): HabitRecord?
    
    @Transaction
    suspend fun checkHabit(record: HabitRecord){
        val existing = findRecord(record.date, record.habitDefinitionId)
        if(existing == null) {
            insert(record)
        }else{
            delete(existing)
        }
    }

}
