package com.example.tracker.ui.daily

import com.example.tracker.data.dto.ConditionGetDailyListDto
import com.example.tracker.data.dto.HabitGetDailyListDto
import com.example.tracker.data.entity.ConditionDefinition
import com.example.tracker.data.entity.ConditionTag
import com.example.tracker.data.entity.ExpenseRecord
import com.example.tracker.data.entity.HabitCategoryDefinition
import com.example.tracker.data.entity.HabitDefinition
import com.example.tracker.data.entity.ItemDefinition
import com.example.tracker.data.model.expenseCategories

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

    val conditionSearchResult: List<ConditionDefinition> =emptyList(), // searchCondition(string: String)
    val conditionForm: ConditionDefinition? = null,
    val tagSearchResult: List<ConditionTag> = emptyList(),
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
    val itemName: String, // user -> search1
    val itemId: Long, // search1
    val subCategoryName: String, // search2
    val subCategoryId: Long, // search2
    val unitPrice: Long, // user -> search1
    val quantity: Int, // user
    val memo: String // user
)



data class HabitCategory(
    val categoryName: String,
    val habitList: List<HabitGetDailyListDto>
)


data class ConditionDailyListByTag(
    val tagName: String,
    val conditionList: List<ConditionGetDailyListDto>
)
