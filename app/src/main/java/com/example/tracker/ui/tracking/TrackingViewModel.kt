package com.example.tracker.ui.tracking

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tracker.data.dao.ConditionRecordDao
import com.example.tracker.data.dao.ExpenseRecordDao
import com.example.tracker.data.dao.HabitRecordDao
import com.example.tracker.data.entity.ConditionTag
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import androidx.compose.runtime.setValue
import com.example.tracker.data.entity.ExpenseSubCategoryDefinition
import com.example.tracker.data.entity.HabitCategoryDefinition
import com.example.tracker.data.entity.HabitDefinition
import com.example.tracker.data.model.IdWithName
import com.example.tracker.data.model.expenseCategories
import java.util.Calendar

class TrackingViewModel (
    private val expenseRecordDao: ExpenseRecordDao, // 주생성자의 매개변수
    private val habitRecordDao: HabitRecordDao,
    private val conditionRecordDao: ConditionRecordDao,
) : ViewModel() {
    var trackingUiState by mutableStateOf(TrackingUiState())
        private set

    private val dateFormat = SimpleDateFormat(
        "yyyy-MM-dd",
        Locale.getDefault()
    )


    init { // 하 기간을 해야하는거군 하 놔
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        changePeriod(today, today)
        loadTrackingData()
    }

    fun changePeriod(startDate: String, endDate: String) {
        trackingUiState = trackingUiState.copy(startDate = startDate, endDate = endDate)
    }

    fun extendPeriodToPast() {
        val startDate = dateFormat.parse(trackingUiState.startDate) ?: return

        val calendar = Calendar.getInstance()
        calendar.time = startDate
        calendar.add(Calendar.DAY_OF_MONTH, -1)

        trackingUiState = trackingUiState.copy(startDate = dateFormat.format(calendar.time))
        reloadTrackingData()
    }

    fun extendPeriodToFuture() {
        val endDate = dateFormat.parse(trackingUiState.endDate) ?: return

        val calendar = Calendar.getInstance()
        calendar.time = endDate
        calendar.add(Calendar.DAY_OF_MONTH, 1)

        trackingUiState = trackingUiState.copy(endDate = dateFormat.format(calendar.time))
        reloadTrackingData()
    }

    fun loadTrackingData() {
        val startDate = trackingUiState.startDate
        val endDate = trackingUiState.endDate

        //---------expense-----------
        trackingUiState = trackingUiState.copy(
            startDate = startDate,
            endDate = endDate,
            selectedCategories = expenseCategories
        )
        calcDailyExpense()

        //----------habit------------

    }

    fun reloadTrackingData() {
        //--------expense------------
        val selectedSubCategories = trackingUiState.selectedSubCategories
        val selectedCategory = trackingUiState.selectedCategory

        calcDailyExpense()
        expenseTracking(selectedSubCategories.map { sub -> sub.id })
        wholeCircleGraphing()
        if (selectedCategory != null) {
            selectCategoryForCircleGraph(selectedCategory)
        }

        //---------habit---------------
    }

    fun selectCategory(categoryId: Long) {
        viewModelScope.launch {
            val expenseSubCategories = expenseRecordDao.getSubByCategoryId(categoryId)
            trackingUiState = trackingUiState.copy(
                expenseSubCategories = expenseSubCategories,
                selectedSubCategories = expenseSubCategories
            )
        }

    }

    fun changeTrackingList(subCategory: ExpenseSubCategoryDefinition) {
        viewModelScope.launch {
            val subCategories = trackingUiState.selectedSubCategories
            if (subCategories.contains(subCategory)) {
                trackingUiState =
                    trackingUiState.copy(selectedSubCategories = subCategories - subCategory)
            } else {
                trackingUiState =
                    trackingUiState.copy(selectedSubCategories = subCategories + subCategory)
            }
        }
    }

    // expenseSubCategories.map{ sub -> sub.id}하고 사용해야함
    fun expenseTracking(subCategories: List<Long>) {
        viewModelScope.launch {
            val start = trackingUiState.startDate
            val end = trackingUiState.endDate

            val tracking = expenseRecordDao.tracking(start, end, subCategories)
            trackingUiState = trackingUiState.copy(expenseTracking = tracking)
        }
    }

    fun wholeCircleGraphing() {
        viewModelScope.launch {
            val start = trackingUiState.startDate
            val end = trackingUiState.endDate

            val graph = expenseRecordDao.wholeCircleGraphing(start, end)
            trackingUiState = trackingUiState.copy(wholeCircleGraphing = graph)
        }
    }

    fun selectCategoryForCircleGraph(category: IdWithName) {
        trackingUiState = trackingUiState.copy(selectedCategory = category)
        viewModelScope.launch {
            val start = trackingUiState.startDate
            val end = trackingUiState.endDate

            val circleGraph = expenseRecordDao.circleGraphingByCategory(start, end, category.id)
            trackingUiState = trackingUiState.copy(circleGraphingByCategory = circleGraph)
        }
    }


    fun changeCategories(category: IdWithName) {
        val categories = trackingUiState.selectedCategories

        if (categories.contains(category)) {
            trackingUiState = trackingUiState.copy(selectedCategories = categories - category)
        } else {
            trackingUiState = trackingUiState.copy(selectedCategories = categories + category)
        }
        calcDailyExpense()
    }

    fun calcDailyExpense() {
        viewModelScope.launch {
            val start = trackingUiState.startDate
            val end = trackingUiState.endDate
            val selectedCategoryIds = trackingUiState.selectedCategories

            val categoryIds =
                if (selectedCategoryIds.isEmpty()) {
                    expenseCategories.map { category -> category.id }
                } else {
                    selectedCategoryIds.map { category -> category.id }
                }


            val graph = expenseRecordDao.calcDailyExpense(start, end, categoryIds)
            trackingUiState = trackingUiState.copy(calcDailyExpense = graph)
        }
    }

    //-------------habit------------------
    // 설정한 기간에 속하는 프로젝트들 반환
    fun projectList() {
        viewModelScope.launch {
            val startDate = trackingUiState.startDate
            val endDate = trackingUiState.endDate

            val projects = habitRecordDao.getHabitProjectsByDate(startDate, endDate)
            trackingUiState = trackingUiState.copy(habitProjects = projects)
        }
    }

    // 선택된 프로젝트로 트래킹
    fun projectTracking(project: HabitCategoryDefinition) {
        viewModelScope.launch {
            val startDate = trackingUiState.startDate
            val endDate = trackingUiState.endDate

            val projectsForTracking =
                habitRecordDao.getMonthlyByCategory(project.id, startDate, endDate)

            trackingUiState = trackingUiState.copy(
                selectedHabitProject = project,
                projectTracking = projectsForTracking
            )
        }
    }

    // 선택된 프로젝트에 속한 데피니션들을 리스트로 나열
    fun habitDefinitionList() {
        viewModelScope.launch {
            val startDate = trackingUiState.startDate
            val endDate = trackingUiState.endDate
            val project = trackingUiState.selectedHabitProject
            val definitionList =
                if(project != null){
                    habitRecordDao.getDefinitionListByProject(listOf(project.id))
                }else{
                    habitRecordDao.getAllDefinitions(startDate, endDate)
                }

            trackingUiState = trackingUiState.copy(habitDefinitions = definitionList)
        }
    }

    fun changeDefinitionList(definition: HabitDefinition){
        var definitions = trackingUiState.habitDefinitions

        if(definitions.contains(definition)){
            definitions = definitions - definition
        } else{
            definitions = definitions + definition
        }

        trackingUiState = trackingUiState.copy(selectedDefinitions = definitions)
    }

    fun definitionTracking(){
        viewModelScope.launch {

        }
    }

}