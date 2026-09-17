package com.example.tracker.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(
    tableName = "condition_relation",
    primaryKeys = ["recordId", "tagId"],
    foreignKeys = [
        ForeignKey(
            entity = ConditionCheckRecord::class,
            parentColumns = ["id"],
            childColumns = ["recordId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = ConditionTag::class,
            parentColumns = ["id"],
            childColumns = ["tagId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index("recordId"),
        Index("tagId")
    ]
)

data class ConditionRelation(

    //@PrimaryKey(autoGenerate = true)

    //val id: Long = 0,

    val recordId: Long,

    val tagId: Long

) // 중간엔티티(관계저장) -> 다대다로 만들 수 있음