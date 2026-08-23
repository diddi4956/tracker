package com.example.tracker.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.tracker.data.entity.ConditionDefinition
import com.example.tracker.data.entity.ConditionDefinitionTag
import com.example.tracker.data.entity.ConditionTag

@Dao
interface ConditionDefinitionDao {
    @Insert
    suspend fun insertDefinition(condition: ConditionDefinition): Long // 근데 자동으로 주키를 반환하게 하는거임? 이렇게 넣으면?

    @Insert
    suspend fun insertTag(tag: ConditionTag)

    @Insert
    suspend fun insertDefinitionTag(relation: List<ConditionDefinitionTag>)

    @Update
    suspend fun updateDefinition(condition: ConditionDefinition)

    @Update
    suspend fun updateTag(tag: ConditionTag)

    @Update
    suspend fun updateDefinitionTag(relation: ConditionDefinitionTag)

    @Delete
    suspend fun deleteDefinition(condition: ConditionDefinition)

    @Delete
    suspend fun deleteTag(tag: ConditionTag)

    @Delete
    suspend fun deleteRelation(relation: ConditionDefinitionTag)

    @Query("SELECT * FROM condition_definition")
    suspend fun getAll(): List<ConditionDefinition>

//    @Query("SELECT * FROM condition_definition WHERE isActive = 1")
//    suspend fun getActiveCondition(): List<ConditionDefinition>

    @Query("SELECT * FROM condition_definition WHERE name = :name")
    suspend fun getByName(name: String):List<ConditionDefinition>

    @Query("SELECT * FROM condition_tag WHERE name = :name")
    suspend fun getByTagName(name: String): List<ConditionTag>

    @Transaction // 태그가 없는 데피니션 생성 불가
    suspend fun insertDefinitionWithTag(definition: ConditionDefinition, tagIds: List<Long>){
        require(tagIds.isNotEmpty()){ // 조건이 false면 실행 그럼 if문쓰면 안되나 굳이 require써야함?
            "최소 한개 이상의 tag 필요"
        }

        val definitionId = insertDefinition(definition) //이거 이해가 잘 안가. 함수가 definitionId를 반환하는기능마저 하는거야? 룸은 대체 어떤것이기에...단순 업뎃만 만들어주는게 아니라고? get함수를 만들어야지만 이게 가능한줄알았는데?
        insertDefinitionTag(tagIds.map({tagId -> ConditionDefinitionTag(conditionDefinitionId = definitionId, tagId = tagId)})) //함수매개변수로 람다 되는거아녔음?
    }

    @Query("DELETE FROM condition_definition_tag WHERE conditionDefinitionId = :definitionId")
    suspend fun deleteRelationByDefinition(definitionId: Long)

    @Transaction// 관계디비 삭제 트랜잭션(태그나 데피니션을 삭제할때 그것이 사용된 관계 디비를 삭제함)
    suspend fun deleteRelationByDefinition(definition: ConditionDefinition){
        // 데피니션 삭제이므로, 이 데피니션id로 릴렐이션을 검색, 삭제하는 함수
        deleteRelationByDefinition(definition.id)
        deleteDefinition(definition)
    }

    @Query("DELETE FROM condition_definition_tag WHERE tagId = :tagId")
    suspend fun deleteRelationByTag(tagId: Long)

    @Transaction
    suspend fun deleteRelationByTag(tag: ConditionTag){
        deleteRelationByTag(tag.id)
        deleteTag(tag)
    }

    @Query("SELECT t.* FROM condition_tag AS t JOIN condition_definition_tag AS r ON t.id = r.tagId " +
            "WHERE r.conditionDefinitionId = :conditionId")
    suspend fun findTagByConditionId(conditionId: Long): List<ConditionTag>

    @Query("SELECT * FROM condition_definition " +
            "WHERE name = :name AND conditionCategoryId = :categoryId AND (:excludeId IS NULL OR id != :excludeId)")
    suspend fun testDuplication(name: String, categoryId: Long, excludeId: Long?): List<ConditionDefinition>
}