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
import com.example.tracker.data.entity.ConditionDefinition
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
        projectList()
        // Todo: 조회 기간과 프로젝트 활동 기간을 비교하여, 활동 기간 밖의 날짜는 '미실천'과 구분되도록 표시하기

        //---------condition-----------
        conditionTrackingOptions()
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
        reProjectTracking()

        //---------condition------------
        conditionTrackingOptions()

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

            // 선택된 프로젝트로 트래킹
            val projectsForTracking =
                habitRecordDao.getMonthlyByCategory(project.id, startDate, endDate)

            // 선택된 프로젝트에 속한 데피니션들을 리스트로 나열
            val definitionList = habitRecordDao.getDefinitionListByProject(listOf(project.id))

            trackingUiState = trackingUiState.copy(
                selectedHabitProject = project,
                projectTracking = projectsForTracking,
                habitDefinitions = definitionList,
                selectedDefinitions = definitionList
            )
            definitionTracking()

        }
    }

    // 리로드용
    fun reProjectTracking(){
        viewModelScope.launch {
            val startDate = trackingUiState.startDate
            val endDate = trackingUiState.endDate
            val selectedHabitProject = trackingUiState.selectedHabitProject

            val habitProjects = habitRecordDao.getHabitProjectsByDate(startDate, endDate)
            trackingUiState = trackingUiState.copy(habitProjects = habitProjects)

            if(selectedHabitProject != null && habitProjects.contains(selectedHabitProject)){
                val projectsForTracking = habitRecordDao.getMonthlyByCategory(
                    selectedHabitProject.id,
                    startDate,
                    endDate)
                trackingUiState = trackingUiState.copy(projectTracking = projectsForTracking)

                definitionTracking()
            } else {
                trackingUiState = trackingUiState.copy(
                    selectedHabitProject = null,
                    projectTracking = emptyList(),
                    habitDefinitions = emptyList(),
                    selectedDefinitions = emptyList(),
                    definitionTracking = emptyList())
            }
        }

    }

    fun changeDefinitionList(definition: HabitDefinition){
        var definitions = trackingUiState.selectedDefinitions

        if(definitions.contains(definition)){
            definitions = definitions - definition
        } else{
            definitions = definitions + definition
        }

        trackingUiState = trackingUiState.copy(selectedDefinitions = definitions)
        definitionTracking()
    }

    fun definitionTracking(){
        viewModelScope.launch {
            val startDate = trackingUiState.startDate
            val endDate = trackingUiState.endDate
            val habitIds = trackingUiState.selectedDefinitions.map{def -> def.id}

            val records = habitRecordDao.trackingByDefinition(startDate, endDate, habitIds)

            trackingUiState = trackingUiState.copy(definitionTracking = records)
            // 세로축: selectedDefinitions, 가로축: definitionTracking
        }
    }

    //---------condition-------------
    fun conditionTrackingOptions(){
        viewModelScope.launch { // 기간 상관없이 모든 옵션들 보여주기
            val conditionDefinitions = conditionRecordDao.getDefinitionFrequency(null, null)
                .map{ data ->
                    ConditionDefinition(
                        id = data.id,
                        name = data.name
                    )
                }
            val conditionTags = conditionRecordDao.getTagFrequency(null, null)
                .map{ data ->
                    ConditionTag(
                        id = data.id,
                        name = data.name
                    )
                }

            trackingUiState = trackingUiState.copy(
                conditionDefinitions = conditionDefinitions,
                conditionTags = conditionTags
            )

            if(trackingUiState.selectedConDefinitions.isNotEmpty()){
                trackingByConDefinitions()
            }

            if(trackingUiState.selectedConTags.isNotEmpty()){
                trackingByTags()
            }

            if(trackingUiState.selectedConDefinition != null){
                graphingTags()
            }

            if(trackingUiState.selectedConTag != null){
                graphingDefinitions()
            }

        }
    }

    fun selectDefinitions(definition: ConditionDefinition){
        var selectedConDefinitions = trackingUiState.selectedConDefinitions

        if(selectedConDefinitions.contains(definition)){
            selectedConDefinitions = selectedConDefinitions - definition
        } else{
            selectedConDefinitions = selectedConDefinitions + definition
        }

        trackingUiState = trackingUiState.copy(selectedConDefinitions = selectedConDefinitions)
        //trackingByConDefinitions 실행 함수 추가
        if(selectedConDefinitions.isNotEmpty()){
            trackingByConDefinitions()
        } else{
            trackingUiState = trackingUiState.copy(trackingByConDefinitions = emptyList())
        }
    }

    fun trackingByConDefinitions(){
        viewModelScope.launch {
            val startDate = trackingUiState.startDate
            val endDate = trackingUiState.endDate
            val selectedConDefinitionIds = trackingUiState.selectedConDefinitions.map { definition -> definition.id }

            if(selectedConDefinitionIds.isNotEmpty()){
                val trackingByConDefinitions =
                    conditionRecordDao.trackingByConDefinitions(selectedConDefinitionIds, startDate, endDate)
                        .groupBy { data -> data.definitionId to data.date }
                        .map{ (key, rows) ->
                            val (definitionId, date) = key
                            A(
                                definitionId,
                                date,
                                rows.mapNotNull{ row -> row.tag }
                            )
                        }

                trackingUiState = trackingUiState.copy(trackingByConDefinitions = trackingByConDefinitions)
            }
        }
    }

    fun selectTags(tag: ConditionTag){
        var selectedConTags = trackingUiState.selectedConTags

        if(selectedConTags.contains(tag)){
            selectedConTags = selectedConTags - tag
        } else{
            selectedConTags = selectedConTags + tag
        }

        trackingUiState = trackingUiState.copy(selectedConTags = selectedConTags)
        // trackingByTags 실행 함수 추가
        if(selectedConTags.isNotEmpty()) {
            trackingByTags()
        } else{
            trackingUiState = trackingUiState.copy(
                trackingByConTags = emptyList()
            )
        }
    }

    fun trackingByTags(){
        viewModelScope.launch {
            val startDate = trackingUiState.startDate
            val endDate = trackingUiState.endDate
            val selectedConTagIds = trackingUiState.selectedConTags.map{tag -> tag.id}

            if(selectedConTagIds.isNotEmpty()){
                val trackingByTags =
                    conditionRecordDao.trackingByConTags(selectedConTagIds, startDate, endDate)
                        .groupBy { data -> data.tagId to data.date  }
                        .map{(key, rows) ->
                            val (tagId, date) = key
                            B(
                                tagId,
                                date,
                                rows.map{ row -> row.definition}
                            )
                        }

                trackingUiState = trackingUiState.copy(trackingByConTags = trackingByTags)
            }
        }
    }

    fun selectDefinition(definition: ConditionDefinition){
        trackingUiState = trackingUiState.copy(selectedConDefinition = definition)
        // graphingTags 실행 함수 추가
        graphingTags()
    }

    fun graphingTags(){
        viewModelScope.launch {
            val startDate = trackingUiState.startDate
            val endDate = trackingUiState.endDate
            val selectedConDefinition = trackingUiState.selectedConDefinition

            if(selectedConDefinition != null){
                val graphingTags = conditionRecordDao.getTagFrequenciesByDefinition(startDate, endDate, selectedConDefinition.id)
                trackingUiState = trackingUiState.copy(graphingTags = graphingTags)
            }
        }
    }

    fun selectTag(tag: ConditionTag){
        trackingUiState = trackingUiState.copy(selectedConTag = tag)
        // graphingDefinitions 실행 함수 추가
        graphingDefinitions()
    }

    fun graphingDefinitions(){
        viewModelScope.launch {
            val startDate = trackingUiState.startDate
            val endDate = trackingUiState.endDate
            val selectedConTag = trackingUiState.selectedConTag

            if(selectedConTag != null){
                val graphingDefinitions = conditionRecordDao.getDefinitionFrequenciesByTag(startDate, endDate, selectedConTag.id)
                trackingUiState = trackingUiState.copy(
                    graphingDefinitions = graphingDefinitions
                )
            }
        }
    }

}