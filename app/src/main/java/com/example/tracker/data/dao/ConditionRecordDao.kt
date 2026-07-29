package com.example.tracker.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.tracker.data.dto.ConditionGetDailyListDto
import com.example.tracker.data.dto.ConditionGetDefinitionListDto
import com.example.tracker.data.dto.ConditionGetMonthlyByTagDto
import com.example.tracker.data.dto.ConditionGetTagListDto
import com.example.tracker.data.dto.ConditionTrackingByDefinitionDto
import com.example.tracker.data.dto.ConditionTrackingByTagDto
import com.example.tracker.data.entity.ConditionCheckRecord
import com.example.tracker.data.entity.ConditionDefinition

@Dao
interface ConditionRecordDao {
    @Insert
    suspend fun insert(record: ConditionCheckRecord)

    @Update
    suspend fun update(record: ConditionCheckRecord)

    @Delete
    suspend fun delete(record: ConditionCheckRecord)

    @Query("SELECT * FROM condition_record")
    suspend fun getAll(): List<ConditionCheckRecord>

    @Query("SELECT * FROM condition_record WHERE id = :recordId")
    suspend fun getByRecordId(recordId: Long): List<ConditionCheckRecord?>

    @Query("SELECT * FROM condition_record WHERE date = :date")
    suspend fun getByDate(date: String): List<ConditionCheckRecord>

    // 트래킹페이지 -1(데피니션별로) -> /dto 수정필요
    @Query("SELECT r.date AS date, r.conditionDefinitionId AS conditionDefinitionId, d.conditionCategoryId AS conditionCategoryId FROM condition_record AS r LEFT JOIN condition_definition AS d ON r.conditionDefinitionId = d.id WHERE date BETWEEN :start AND :end")
    suspend fun trackingByDefinition(
        start: String,
        end: String
    ): List<ConditionTrackingByDefinitionDto>

    @Query(
        "SELECT d.name AS conditionDefinitionName, d.id AS definitionId, t.id AS tagId, t.name AS tagName " +
                "FROM condition_definition AS d LEFT JOIN condition_definition_tag AS dt ON d.id = dt.conditionDefinitionId INNER JOIN condition_tag AS t ON dt.tagId = t.id " +
                "WHERE t.id IN (:tagIds) ORDER BY d.name ASC"
    )
    suspend fun getDefinitionList(tagIds: List<Long>): List<ConditionGetDefinitionListDto>

    // 트래킹페이지 -2(태그별로) -> /dto수정필요
    @Query(
        "SELECT COUNT(r.id) AS checkedCount, r.date AS date, r.conditionDefinitionId AS conditionDefinitionId, d.name AS definitionName, t.name AS tagName, d.conditionCategoryId AS conditionCategoryId " +
                "FROM condition_tag AS t LEFT JOIN condition_definition_tag AS dt ON t.id = dt.tagId LEFT JOIN condition_definition AS d ON dt.conditionDefinitionId = d.id LEFT JOIN condition_record AS r ON r.conditionDefinitionId = d.id AND r.date BETWEEN :start AND :end " +
                "WHERE t.id IN (:tagIds) " +
                "GROUP BY r.date, t.id, t.name " +
                "ORDER BY d.name ASC"
    )
    suspend fun trackingByTag(
        tagIds: List<Long>,
        start: String,
        end: String
    ): List<ConditionTrackingByTagDto>

    @Query("SELECT name, id FROM condition_tag WHERE id IN (:tagIds)")
    suspend fun getTagList(tagIds: List<Long>): List<ConditionGetTagListDto>

    // 먼슬리로 추이 보기
    @Query(
        "SELECT r.date AS date, COUNT(r.id) AS countOfRecord " +
                "FROM condition_record AS r INNER JOIN condition_definition AS d ON r.conditionDefinitionId = d.id INNER JOIN condition_definition_tag AS dt ON d.id = dt.conditionDefinitionId INNER JOIN condition_tag AS t ON dt.tagId = t.id " +
                "WHERE t.id = :tagId AND r.date BETWEEN :start AND :end " +
                "GROUP BY r.date"
    )
    suspend fun getMonthlyByTag(
        tagId: Long,
        start: String,
        end: String
    ): List<ConditionGetMonthlyByTagDto>

    // 데일리 체크리스트 -> /dto 수정필요
    @Query(
        "SELECT d.id AS id, t.id AS tagId, d.name AS name,  CASE WHEN r.id IS NULL THEN 0 ELSE 1 END AS checked " +
                "FROM condition_tag AS t INNER JOIN condition_definition_tag AS dt ON t.id = dt.tagId INNER JOIN condition_definition AS d ON dt.conditionDefinitionId = d.id LEFT JOIN condition_record AS r ON d.id = r.conditionDefinitionId AND r.date = :date " + //그 조건을 조인할때 부를지 조인하고 최종 디비에서 부를지의 차이
                "WHERE t.id IN (:tagIds) " +
                "ORDER BY t.name, d.name"
    ) // 데피니션이 최소 태그 하나는 갖게하고싶은데...엔티티를 어떻게 해야할까 -> 엔티티보단 트랜잭션으로 묶기
    suspend fun getDailyList(date: String, tagIds: List<Long>): List<ConditionGetDailyListDto>

    @Update
    suspend fun updateDefinition(condition: ConditionDefinition)

    @Query("SELECT * FROM condition_definition WHERE id = :conditionDefinitionId")
    suspend fun getDefinitionById(conditionDefinitionId: Long): ConditionDefinition

    @Query("SELECT * FROM condition_record WHERE date = :date AND conditionDefinitionId = :conditionDefinitionId LIMIT 1") //근데 이거 uique건걸로만 해야하나? 하긴 더 하는거면 에너지낭비긴해
    suspend fun findSameConditionRecord(
        date: String,
        conditionDefinitionId: Long
    ): ConditionCheckRecord?

    @Query("DELETE FROM condition_record WHERE id = :recordId")
    suspend fun deleteRecordById(recordId: Long)

    @Transaction
    suspend fun checkingRecordAndDefinitionFrequency(record: ConditionCheckRecord) {
        val existing = findSameConditionRecord(record.date, record.conditionDefinitionId)
        val definition = getDefinitionById(record.conditionDefinitionId)

        if (existing == null) { //없으면 새로 만들어야함
            insert(record)
            updateDefinition(definition.copy(frequency = definition.frequency + 1))
        } else {
            deleteRecordById(record.id)
            updateDefinition(definition.copy(frequency = definition.frequency - 1))
        }
    }
}
/*
    할것
    /1. 트래킹페이지에 카테고리별로 보기 추가 -> 굳이? 카테고리는 ui를 위한거지 트래킹을 위한게 아니라 삭제
    /2. 트래킹페이지(2)에 카테고리별로 색깔입히기 추가 즉, 카테고리id도 끌고옴 -> 트래킹2에서 카테고리 아이디는 크게 필요없을듯하지만 일단 넣어둠
    /3. 리코드 불러오는 방식 대폭 수정(daily)(checked를 없앴음. 그래서 조회해서 있으면 체크 없으면 체크안함으로 바꿔야함 habit의 dao를 참조) : LEFT JOIN으로 바꾸기
    4. 트랜잭션 추가
}*/