package com.example.tracker.ui.tracking

import androidx.room.Embedded
import com.example.tracker.data.dto.ConditionRecordWithTags
import com.example.tracker.data.dto.DefinitionTracking
import com.example.tracker.data.dto.ExpenseCircleByCategoryDto
import com.example.tracker.data.dto.ExpenseDailyPriceDto
import com.example.tracker.data.dto.ExpenseTrackingDto
import com.example.tracker.data.dto.ExpenseWholeCircleDto
import com.example.tracker.data.dto.IdAndFrequencyDto
import com.example.tracker.data.dto.ProjectTracking
import com.example.tracker.data.entity.ConditionDefinition
import com.example.tracker.data.entity.ConditionTag
import com.example.tracker.data.entity.ExpenseSubCategoryDefinition
import com.example.tracker.data.entity.HabitCategoryDefinition
import com.example.tracker.data.entity.HabitDefinition
import com.example.tracker.data.entity.HabitRecord
import com.example.tracker.data.model.IdWithName
import com.example.tracker.data.model.expenseCategories
import java.util.concurrent.locks.Condition

data class TrackingUiState (

    // val date: String ="",
    val startDate: String = "",
    val endDate: String = "",
    // expense
    val expenseSubCategories : List<ExpenseSubCategoryDefinition> = emptyList(),
    // 기본 서브카테고리 리스트(보여주기용)

    val selectedSubCategories: List<ExpenseSubCategoryDefinition> = emptyList(),
    // 트래킹을 위해 선택된 서브카테고리들

    val expenseTracking: List<ExpenseTrackingDto> = emptyList(),


    val wholeCircleGraphing: List<ExpenseWholeCircleDto> = emptyList(),

    // 카테고리별 원그래프를 위해 선정된것.
    val selectedCategory: IdWithName? = null,
    val circleGraphingByCategory: List<ExpenseCircleByCategoryDto> = emptyList(),

    val categoryList: List<IdWithName> = expenseCategories,
    val selectedCategories: List<IdWithName> = emptyList(),
    val calcDailyExpense: List<ExpenseDailyPriceDto> = emptyList(),

    // habit
    /*
B. 그 기간, 프로젝트 안에서 몇개 했는가
1) 프로젝트 선택
==> 삭제

Init> 그 기간과 겹치는 프로젝트들이 전부 뜸.

C. 선택한 프로젝트의 날짜별 실천 개수
1) 프로젝트 선택
2) 일간 횟수 불러오기

-> 그 프로젝트의 해빗들이 쭉 뜸

A. 그 기간, 그 해빗을 했는가
1) 데피니션 선택
     */
    val habitProjects: List<HabitCategoryDefinition> = emptyList(), // loadTrackingPage()
    val selectedHabitProject: HabitCategoryDefinition? = null,
    val projectTracking: List<ProjectTracking> = emptyList(),

    val habitDefinitions: List<HabitDefinition> = emptyList(),
    val selectedDefinitions: List<HabitDefinition> = emptyList(),
    val definitionTracking: List<DefinitionTracking> = emptyList(),

    // condition
    // 기본 선택지 목록
    val conditionDefinitions: List<ConditionDefinition> = emptyList(), // load
    val conditionTags : List<ConditionTag> = emptyList(), // load

    // 선택된 목록 -> 날짜별로 리코드 트래킹
    val selectedConDefinitions: List<ConditionDefinition> = emptyList(),
    val selectedConTags: List<ConditionTag> = emptyList(),

    val trackingByConDefinitions: List<A> = emptyList(),
    val trackingByConTags: List<B> = emptyList(),

    //선택된 대상(딱 하나만) -> 기간별 데피니션/태그에 딸린 태그s/데피니션s 의 각 누적 횟수
    val selectedConDefinition: ConditionDefinition? = null,
    val selectedConTag: ConditionTag? = null,

    val graphingTags: List<C> = emptyList(),
    val graphingDefinitions: List<C> = emptyList()
    )

// Todo: A, B, C 어쩌구 클래스들 정리(9.24)
data class A(
    val definitionId: Long,
    val date: String,
    val tags: List<ConditionTag> = emptyList()
)

data class A1(
    val definitionId: Long,
    val date: String,
    @Embedded
    val tag: ConditionTag?
)

data class B(
    val tagId: Long,
    val date: String,
    val definitions: List<ConditionDefinition> = emptyList()
)

data class B1(
    val tagId: Long,
    val date: String,
    @Embedded
    val definition: ConditionDefinition
)

data class C(
    val id: Long,
    val name: String,
    val count: Long
)



