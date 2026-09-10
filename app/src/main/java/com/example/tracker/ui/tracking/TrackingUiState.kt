package com.example.tracker.ui.tracking

import com.example.tracker.data.dto.ConditionGetDefinitionListDto
import com.example.tracker.data.dto.ConditionGetMonthlyByTagDto
import com.example.tracker.data.dto.ConditionGetTagListDto
import com.example.tracker.data.dto.ConditionTrackingByDefinitionDto
import com.example.tracker.data.dto.ConditionTrackingByTagDto
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
import com.example.tracker.data.model.ExpenseCategory
import com.example.tracker.data.model.expenseCategories

data class TrackingUiState (

    // val date: String ="",
    val startDate: String = "",
    val endDate: String = "",
    // expense
    val expenseTrackingOption: List<String> = emptyList(), // loadTrackingData() {체크형리스트, 카테고리별 원그래프 등등의 리스트}

    val selectCategory: List<ExpenseCategory> = expenseCategories, // loadTrackingData() {tracking, circleGraphingByCategory공용}

    val expenseTracking: List<ExpenseTrackingDto> = emptyList(),
    // val subCategory: List<String>, // circleGraphingByCategory에서 빼서 쓰기..

    val circleGraphingByCategory: List<ExpenseCircleByCategoryDto> = emptyList(),

    val wholeCircleGraphing: List<ExpenseWholeCircleDto> = emptyList(),

    val calcDailyExpense: List<ExpenseDailyPriceDto> = emptyList(),

    // habit
    val habitTrackingByDefinition: List<HabitTrackingByDefinitionDto> = emptyList(), // definition 리스트가 필요...한가?
    val habitDefinitionList: List<HabitGetDefinitionListDto> = emptyList(), // 세로 근데 이걸로 또 name: String만 추출해서 선택지로 활용할수도있나?

    val habitTrackingByCategory: List<HabitTrackingByCategoryDto> = emptyList(),
    val habitCategoryList: List<HabitGetCategoryListDto> = emptyList(), // 세로

    val monthlyByCategory: List<HabitGetMonthlyByCategoryDto> = emptyList(),

    // condition
    val conditionTrackingByDefinition: List<ConditionTrackingByDefinitionDto> = emptyList(),// 데피니션 선택 기능이 없음 이것은 데피니션id로 조회한 상세 기록 즉 가로축
    val conditionDefinitionList: List<ConditionGetDefinitionListDto> = emptyList(), // 세로축으로 각 데피니션에 대한 데이터. 태그를 사용하여 색을 바꿈
    // 굳이 세로축 가로축 나눈 이유는 기록이 하나도 없는 데피니션도 축이나 선택지에 표시기위해..

    val getConditionTagList: List<ConditionTag> = emptyList(), // loadTrackingData() {태그들을 선택할 수 있는 선택지}

    val conditionTrackingByTag: List<ConditionTrackingByTagDto> = emptyList(), // 가로축
    val conditionTagList: List<ConditionGetTagListDto> = emptyList(), // 세로축

    val conditionMonthlyByTag: List<ConditionGetMonthlyByTagDto> = emptyList()
    )

