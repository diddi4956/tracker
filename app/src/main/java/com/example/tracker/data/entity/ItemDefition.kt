package com.example.tracker.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey // 어제 분명히 객체 공유와 코드 공유 뭐 그런걸 했는데 기억이 안남....
// 임포트 가지고는 다른 객체의 함수를 가지고 올 순 없다 근데 타입을 가져올 순 있다? 였나?

@Entity(tableName = "item_definition")
data class ItemDefinition(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val subCategoryId: Long, // 근데 생각해보니 서브는 있는데 그냥 익스펜스 데피니션은 안쓰나? 아 서브가 이미 갖고있구나!!
    val name: String,
    val unit: String? = null,
    val kcalPerUnit: Int? = null,
    val isDefault: Boolean = false, // 이건 왜 넣는거야
    val isActive: Boolean = true // 얘도?
    /*
    isDefault : 앱이 기본 제공하는 항목인지 여부. 사용자가 추가한것인지 기본 제공인지.
    isActive : 지금 사용중인지 여부 예전에 쓰던 항목인데 지금은 숨기고싶음 이런거. 삭제는 안하고 비활성화.
     */
)
// 여기엔 가격을 넣지 않는게 좋으려나ㅋㅋ 그럼 칼로리도 여기에 안넣는게 좋지않나 싶었는데 칼로리는 음식별로 다르기도 하니까 여기에 넣어야겠당... 음식고유의 것이니까 칼로리는
// 근데 가격은 어디서 구매했는지에 따라서도 달라질수있으니까 여기에 안넣는게 좋으려나. 그럼 구입처 이런것도 넣는게 좋으려나 오히려? 구입처를 여기에 넣고...흠 일단 생각해보자 지피티랑 상의해야지

/* 구입처는 나중에 차차 추가하는걸로 */