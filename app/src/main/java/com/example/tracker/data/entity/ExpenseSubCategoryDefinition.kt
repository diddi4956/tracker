package com.example.tracker.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey // 이렇게 디폴트하면 그 이름을 쓸 수 있다고?
/* import는 객체를 가져오는게 아니라 이름을 짧게 쓰게 해주는것. 안하면 풀네임을 적어줘야함*/

@Entity(tableName = "expense_subcategory_definition")
data class ExpenseSubCategoryDefinition( // 이건 신기하네 왜 class가 아니고{}도 아니고 ()인거지?
    /*()는 주 생성자+프로퍼티 선언할때 쓰는거. {}는 함수나 추가 코드*/
    /* 이건 코틀린 문법. 데이터 담는 용도의 클래스라서 그럼. 데이터클래스는 자동으로 toString() equals() hashCode() copy()같은걸 만들어줌. 즉 엔티티처럼 데이터 보관용일때 자주 씀 뭔소리임 */
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val categoryId: Long, // 상위 카테고리 연결용인데 이걸 연결하는지 어케 알지 이거 어디서 처리해야하지? dao?
    /* 여긴 그냥 설계만 해두고 매칭은 dao쿼리나 앱 로직에서 처리함. */
    val name: String,
    val isDefault: Boolean = false,
    val isActive: Boolean = true
)