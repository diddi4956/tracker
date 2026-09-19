package com.example.tracker.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.tracker.data.dto.ConRecordWithDefinitionTags
import com.example.tracker.data.dto.ConditionCheckRecordAndDefinitionName
import com.example.tracker.data.dto.IdAndFrequencyDto
import com.example.tracker.data.entity.ConditionCheckRecord
import com.example.tracker.data.entity.ConditionDefinition
import com.example.tracker.data.entity.ConditionRelation
import com.example.tracker.data.entity.ConditionTag

@Dao
interface ConditionRecordDao {
    // --------record---------------
    @Insert
    suspend fun insert(record: ConditionCheckRecord): Long

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

    //--------definition-------------
    @Insert
    suspend fun insertDefinition(condition: ConditionDefinition): Long // 근데 자동으로 주키를 반환하게 하는거임? 이렇게 넣으면?

    @Update
    suspend fun updateDefinition(condition: ConditionDefinition)

    @Delete
    suspend fun deleteDefinition(condition: ConditionDefinition)

    //--------tag------------------
    @Insert
    suspend fun insertTag(tag: ConditionTag)

    @Update
    suspend fun updateTag(tag: ConditionTag)

    @Delete
    suspend fun deleteTag(tag: ConditionTag)

    //------------relation----------
    @Insert
    suspend fun insertRelation(relation: ConditionRelation)

    @Update
    suspend fun updateRelation(relation: ConditionRelation)

    @Delete
    suspend fun deleteRelation(relation: ConditionRelation)

    // -----------tracking-------------
    // 선택된 definition들의 record 트래킹
    @Query("SELECT * " +
            "FROM condition_record " +
            "WHERE conditionDefinitionId IN (:definitionIds) AND date BETWEEN :start AND :end")
    suspend fun getRecordsByDefinitions(definitionIds: List<Long>, start: String, end: String): List<ConditionCheckRecord>

    // 선택된 tag들의 record 트래킹
    @Query("SELECT DISTINCT rc.* " +
            "FROM condition_relation AS rl LEFT JOIN  condition_record AS rc ON rl.recordId = rc.id "+
            "WHERE tagId IN (:tagIds) AND date BETWEEN :start AND :end")
    suspend fun getRecordsByTags(tagIds: List<Long>, start: String, end: String): List<ConditionCheckRecord> // 데피니션 정보는 쿼리 하나로 처리하려하지말고 뷰모델에서 조합하는게 좋은듯?

    // 레코드로 tag들 정보 갖기(결과는 릴레이션)
    @Query("SELECT rl.* " +
            "FROM condition_relation AS rl LEFT JOIN condition_record AS rc ON rl.recordId = rc.id " +
            "WHERE rl.recordId = :recordId")
    suspend fun getRelationByRecord(recordId: Long): List<ConditionRelation>

    @Query("SELECT * FROM condition_tag WHERE id = :tagId ")
    suspend fun getTagByTagId(tagId: Long): ConditionTag?


    //-------데일리화면-------------
    // 입력된 날짜에 체크된 목록 가져오기
    @Query("SELECT rc.*, d.name AS definitionName " +
            "FROM condition_record AS rc LEFT JOIN condition_definition AS d ON rc.conditionDefinitionId = d.id " +
            "WHERE date = :date " +
            "ORDER BY d.name ASC")
    suspend fun getCheckedRecordByDate(date: String): List<ConditionCheckRecordAndDefinitionName> // A: List<conditionRecord(recordId포함), definitionName>

    @Query("SELECT t.*, r.recordId AS recordId " +
            "FROM condition_relation AS r INNER JOIN condition_tag AS t ON r.tagId = t.id " +
            "WHERE r.recordId IN (:recordIds)" )
    suspend fun getTagsByRecordIds(recordIds: List<Long>): List<ConRecordWithDefinitionTags> // B: List<recordId, <tags>>(형태는 아님)  ==> dailyConditions: List<ConditionRecordWithTags> = List<A,<B>>

    // 데피니션id와 등록 수 저장
    @Query("SELECT conditionDefinitionId AS id, COUNT(id) AS frequency " +
            "FROM condition_record " +
            "WHERE (:start IS NULL OR date >= :start) AND (:end IS NULL OR date <= :end) " +
            "GROUP BY conditionDefinitionId ORDER BY frequency DESC")
    suspend fun getDefinitionFrequency(start: String?, end: String?): List<IdAndFrequencyDto>

    // 태그id와 등록 수 저장
    @Query("SELECT rl.tagId AS id, COUNT(recordId) AS frequency " +
            "FROM condition_record AS rc JOIN condition_relation AS rl ON rc.id = rl.recordId  " +
            "WHERE tagId IN (:tagIds) AND (:start IS NULL OR rc.date >= :start) AND (:end IS NULL OR rc.date <= :end) " +
            "GROUP BY tagId ORDER BY frequency DESC")
    suspend fun getTagFrequency(tagIds: List<Long>, start: String?, end: String?): List<IdAndFrequencyDto>



    // 네임으로서치(definition) ame LIKE '%' || :keyword || '%'"
    @Query("SELECT * FROM condition_definition WHERE name LIKE '%' || :keyword || '%'")
    suspend fun searchDefinitions(keyword: String): List<ConditionDefinition>

    // 네임으로서치(tag)
    @Query("SELECT * FROM condition_tag WHERE name LIKE '%' || :keyword || '%'")
    suspend fun searchTags(keyword: String): List<ConditionTag>

    // id로 서치(데피니션)
    @Query("SELECT * FROM condition_definition WHERE id = :definitionId")
    suspend fun getDefinitionsById(definitionId: Long): List<ConditionDefinition>

    // id로 서치(태그(
    @Query("SELECT * FROM condition_tag WHERE id = :tagId")
    suspend fun getTagsById(tagId: Long)

    // 데피니션 중복 체크
    @Query("SELECT * FROM condition_definition " +
            "WHERE (:excludeId IS NULL OR id != :excludeId) AND name = :name")
    suspend fun defDuplicationTest(excludeId: Long?, name: String): List<ConditionDefinition>

    // 태그 중복 체크
    @Query("SELECT * FROM condition_tag " +
            "WHERE (:excludeId IS NULL OR id != :excludeId) AND name = :name")
    suspend fun tagDuplicationTest(excludeId: Long?, name: String): List<ConditionTag>

    // 같은 리코드 찾기(리코드 체크된지 아닌지 확인)
    @Query("SELECT * FROM condition_record " +
            "WHERE date = :date AND conditionDefinitionId = :definitionId LIMIT 1")
    suspend fun findSameRecord(date: String, definitionId: Long): ConditionCheckRecord?

    @Query("SELECT * FROM condition_relation " +
            "WHERE recordId = :recordId")
    suspend fun getRelationsByRecordId(recordId: Long): List<ConditionRelation>

    @Query("SELECT * FROM condition_definition WHERE id = :conditionDefinitionId")
    suspend fun getDefinitionById(conditionDefinitionId: Long): ConditionDefinition

    @Query("DELETE FROM condition_record WHERE id = :recordId")
    suspend fun deleteRecordById(recordId: Long)

}
