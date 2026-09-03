package com.example.tracker.ui.daily

import com.example.tracker.data.dto.ConditionGetDailyListDto
import com.example.tracker.data.dto.HabitGetDailyListDto
import com.example.tracker.data.entity.ConditionDefinition
import com.example.tracker.data.entity.ConditionTag
import com.example.tracker.data.entity.ExpenseRecord
import com.example.tracker.data.entity.HabitCategoryDefinition
import com.example.tracker.data.entity.HabitDefinition
import com.example.tracker.data.entity.ItemDefinition

data class DailyUiState(
    val date: String = "", // loadDailyData()
    // expense
    val dailyExpenses: List<ExpenseByCategory> = emptyList(), // loadDailyData()

    val expenseRecordForm: ExpenseRecordForm? = null,
    val itemCandidates: List<ItemDefinition> = emptyList(),
    // val showDuplicateDialog: Boolean = false,

    val itemForm: ItemDefinition? = null,

    // habit
    val dailyHabits: List<HabitCategory> = emptyList(), // loadDailyData()

    val updateHabitCategory: HabitCategoryDefinition? = null,
    val updateHabit: HabitDefinition? = null,

    // condition
    val dailyConditions: List<ConditionDailyListByTag> = emptyList(), // loadDailyData()
    val conditionDefinitionListNotChecked: List<ConditionDefinition> = emptyList(), // loadDailyData()

    val conditionSearchText: List<ConditionDefinition> =emptyList(), // searchCondition(string: String)
    val conditionForm: ConditionDefinition? = null,
    val tagList: List<ConditionTag> = emptyList(),
    val conditionTagForm: ConditionTag? = null, // 기본값이 없으면 DailyUiState()라고 빈 초기 상태를 기본기없이 만들수가 없음 아 이게 지금 생성자라서 그런가
    //
    val isLoading: Boolean = false
)
data class ExpenseDailyRecord(
    val recordId: Long?,
    val categoryName: String,
    val subCategoryId: Long,
    val categoryId: Long,
    val itemName: String,
    val totalPrice: Int,
    val memo: String
    // val checked: Boolean
)

data class ExpenseByCategory(
    val categoryName: String,
    // val categoryId: Long,
    val recordList: List<ExpenseDailyRecord>,
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
