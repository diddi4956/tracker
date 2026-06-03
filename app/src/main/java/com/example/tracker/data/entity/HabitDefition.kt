package com.example.tracker.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "habit_definition")
data class HabitDefinition(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val categoryId: Long,
    val name: String,
    val importance: Int,
    val isActive: Boolean = true
)
// 근데 해빗도 카테고리가 필요하지 않을까 싶기도해..아니면 우선순위로 해야하나? 우선순위 키워드로 카테고리를 나누는거지.
// 카테고리가 애초에 우선순위인거. 가장 생활, 목표 이런식으로 나뉘겟지만 이걸 가장 우선시 해야한다! 하는것들 순으로 나누는거지.
// 근데 그거 외에도 카테고리를 나누면 보기 편할거같긴하네...근데 복잡해지려나...그냥 우선순위만 넣으면 좋을지두?
// 우선순위보다는 중요도를 표시해서 그거에 따라서 우선순위를 직접 매겨주는거지!
// 기간도 필요할거같은데? 해빗은? 그 기간이 데피니션에 들어가야하지않을까싶은데ㅠ 리코드에 들어가면 안되잖아..

/* 일단은 이렇게 해두고 기간과 우선순위는 나중에 추가하는 방식으로 진행. 여기에 추가를 하는걸로 진행이 이미 가능하기때문에
나중에 해도 무방
 */