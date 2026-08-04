package com.example.tracker.ui.daily

import com.example.tracker.data.dto.ConditionGetDailyListDto
import com.example.tracker.data.dto.HabitGetDailyListDto
import com.example.tracker.data.entity.ConditionDefinition
import com.example.tracker.data.entity.ConditionTag
import com.example.tracker.data.entity.HabitCategoryDefinition
import com.example.tracker.data.entity.HabitDefinition
import com.example.tracker.data.entity.ItemDefinition

data class DailyUiState(
    val date: String = "",
    // expense
    val expenses: List<ExpenseCategory> = emptyList(),

    val updateRecord: UpdateRecord? = null,
    val itemList: List<String>, // item list

    val updateItem: ItemDefinition? = null,

    // habit
    val habits: List<HabitCategory> = emptyList(),

    val updateHabitCategory: HabitCategoryDefinition? = null,
    val updateHabit: HabitDefinition? = null,

    // condition
    val conditions: List<ConditionDailyListByTag> = emptyList(),
    val conditionSearchText: String = "", // 어 이렇게 하는게 맞는건가? 리스트들이 떠야하는데///
    //val conditionSearchText: List<String> 이렇게 아닌가? 이것은 혹시 몰라 만들어둔 검색창으로 쓰기.
    val updateConditionDefinition: ConditionDefinition? = null,
    val updateConditionTag: ConditionTag? = null, // 기본값이 없으면 DailyUiState()라고 빈 초기 상태를 기본기없이 만들수가 없음 아 이게 지금 생성자라서 그런가
    //
    val isLoading: Boolean = false
)
data class ExpenseDailyRecordByCategory(
    val recordId: Long?,
    // val categoryName: String,
    val subCategoryId: Long,
    val itemName: String,
    val totalPrice: Int,
    val checked: Boolean
)

data class ExpenseCategory(
    val categoryName: String,
    val recordList: List<ExpenseDailyRecordByCategory>,
    val totalPrice: Int
)

data class UpdateRecord(
    val itemName: String, // 근데 이거 검색해서 주루룩 나오게 하는건데 그럼 이것도 리스트가 필요한 영역인가?
    val subCategoryName: String,
    val unitPrice: Int,
    val quantity: Int
)

data class HabitCategory(
    val categoryName: String,
    val habitList: List<HabitGetDailyListDto>
)


data class ConditionDailyListByTag(
    val tagName: String,
    val conditionList: List<ConditionGetDailyListDto>
)