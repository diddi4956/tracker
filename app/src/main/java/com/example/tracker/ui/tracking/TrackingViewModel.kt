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
import com.example.tracker.data.dao.ConditionDefinitionDao

class TrackingViewModel (
    private val expenseRecordDao: ExpenseRecordDao, // 주생성자의 매개변수
    private val habitRecordDao: HabitRecordDao,
    private val conditionRecordDao: ConditionRecordDao,
    private val conditionDefinitionDao: ConditionDefinitionDao

) : ViewModel(){
    var trackingUiState by mutableStateOf(TrackingUiState())
        private set

    init { // 하 기간을 해야하는거군 하 놔
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault() ). format (Date())
        changeDate(today)
    }

    fun changeDate(date: String){
        trackingUiState = trackingUiState.copy(date = date)
        loadTrackingData()
    }

    fun loadTrackingData(){
        viewModelScope.launch{
            val date = trackingUiState.date
            val selectCategory =
            val conditionTagList

            trackingUiState = trackingUiState.copy(date = , selectCategory = , getConditionTagList = )
        }
    }

    // expense
    fun expenseTracking(start: String, end: String){
        viewModelScope.launch{
            val tracking = expenseRecordDao.tracking(start, end)
            trackingUiState = trackingUiState.copy(expenseTracking = tracking)
        }
    }

    fun circleGraphingByCategory(start: String, end: String, categoryId: Long){
        viewModelScope.launch{
            val circleGraph = expenseRecordDao.circleGraphingByCategory(start, end, categoryId)
            trackingUiState = trackingUiState.copy(circleGraphingByCategory = circleGraph)
        }
    }

    fun wholeCircleGraphing(){
        viewModelScope.launch{

            trackingUiState = trackingUiState.copy(wholeCircleGraphing =)
        }
    }

    fun calcDailyExpense(){
        viewModelScope.launch{

            trackingUiState = trackingUiState.copy(calcDailyExpense= )
        }
    }

    // habit
    fun habitTrackingByDefinition(){
        viewModelScope.launch{

            trackingUiState = trackingUiState.copy(habitTrackingByDefinition = , habitDefinitionList = )
        }
    }

    fun habitTrackingByCategory(){
        viewModelScope.launch {

            trackingUiState = trackingUiState.copy(habitTrackingByCategory = , habitCategoryList = )
        }
    }

    fun monthlyByCategory(){
        viewModelScope.launch {

            trackingUiState = trackingUiState.copy(monthlyByCategory = )
        }
    }

    // condition
    fun conditionTrackingByDefinition(){
        viewModelScope.launch {

            trackingUiState = trackingUiState.copy(conditionTrackingByDefinition = , conditionDefinitionList = )
        }
    }

    fun conditionTrackingByTag(tag: List<ConditionTag>){ // getConditionTagList의 결과를 받음
        viewModelScope.launch{

            trackingUiState = trackingUiState.copy(conditionTrackingByTag = , conditionTagList = )
        }
    }

    fun conditionMonthlyByTag(){
        viewModelScope.launch{

            trackingUiState = trackingUiState.copy(conditionMonthlyByTag = )
        }
    }
}