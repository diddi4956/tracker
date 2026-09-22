package com.example.tracker.ui.tracking

import com.example.tracker.data.dto.ExpenseCircleByCategoryDto
import com.example.tracker.data.dto.ExpenseDailyPriceDto
import com.example.tracker.data.dto.ExpenseTrackingDto
import com.example.tracker.data.dto.ExpenseWholeCircleDto
import com.example.tracker.data.dto.HabitGetCategoryListDto
import com.example.tracker.data.dto.HabitGetDefinitionListDto
import com.example.tracker.data.dto.HabitGetMonthlyByCategoryDto
import com.example.tracker.data.dto.HabitTrackingByCategoryDto
import com.example.tracker.data.dto.HabitTrackingByDefinitionDto
import com.example.tracker.data.entity.ConditionTag
import com.example.tracker.data.entity.ExpenseSubCategoryDefinition
import com.example.tracker.data.model.IdWithName
import com.example.tracker.data.model.expenseCategories

data class TrackingUiState (

    // val date: String ="",
    val startDate: String = "",
    val endDate: String = "",
    // expense
    val expenseSubCategories : List<ExpenseSubCategoryDefinition> = emptyList(),
    val selectedSubCategories: List<ExpenseSubCategoryDefinition> = emptyList(),

    val expenseTracking: List<ExpenseTrackingDto> = emptyList(),
    // val subCategory: List<String>, // circleGraphingByCategory에서 빼서 쓰기..

    val wholeCircleGraphing: List<ExpenseWholeCircleDto> = emptyList(),

    val clickedCategory: IdWithName? = null,
    val circleGraphingByCategory: List<ExpenseCircleByCategoryDto> = emptyList(),

    val categoryList: List<IdWithName> = expenseCategories,
    val selectCategory: List<IdWithName> = emptyList(),
    val calcDailyExpense: List<ExpenseDailyPriceDto> = emptyList(),

    // habit
    val habitTrackingByDefinition: List<HabitTrackingByDefinitionDto> = emptyList(), // definition 리스트가 필요...한가?
    val habitDefinitionList: List<HabitGetDefinitionListDto> = emptyList(), // 세로 근데 이걸로 또 name: String만 추출해서 선택지로 활용할수도있나?

    val habitTrackingByCategory: List<HabitTrackingByCategoryDto> = emptyList(),
    val habitCategoryList: List<HabitGetCategoryListDto> = emptyList(), // 세로

    val monthlyByCategory: List<HabitGetMonthlyByCategoryDto> = emptyList(),

    // condition

    )


