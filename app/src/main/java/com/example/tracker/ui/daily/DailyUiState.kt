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

    val expenseRecordForm: ExpenseRecordForm? = null,
    val itemCandidates: List<ItemDefinition> = emptyList(),
    // val showDuplicateDialog: Boolean = false,

    val itemForm: ItemDefinition? = null,

    // habit
    val habits: List<HabitCategory> = emptyList(),

    val updateHabitCategory: HabitCategoryDefinition? = null,
    val updateHabit: HabitDefinition? = null,

    // condition
    val conditions: List<ConditionDailyListByTag> = emptyList(),
    val conditionSearchText: String = "", // 어 이렇게 하는게 맞는건가? 리스트들이 떠야하는데///
    //val conditionSearchText: List<String> 이렇게 아닌가? 이것은 혹시 몰라 만들어둔 검색창으로 쓰기.
    val conditionForm: ConditionDefinition? = null,
    val tagList: List<ConditionTag>,
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

data class ExpenseRecordForm(
    val recordId: Long,
    val itemName: String, // 근데 이거 검색해서 주루룩 나오게 하는건데 그럼 이것도 리스트가 필요한 영역인가?
    val itemId: Long,
    val subCategoryName: String,
    val subCategoryId: Long,
    val unitPrice: Long,
    val quantity: Int
    // State에는 화면에 직접 안 보이는 값이 있어도 돼. State는 “화면에 글자로 출력할 값 목록”이 아니라 현재 UI가 동작하기 위해 필요한 데이터 전체라고 보면 돼.
)



data class HabitCategory(
    val categoryName: String,
    val habitList: List<HabitGetDailyListDto>
)


data class ConditionDailyListByTag(
    val tagName: String,
    val conditionList: List<ConditionGetDailyListDto>
)