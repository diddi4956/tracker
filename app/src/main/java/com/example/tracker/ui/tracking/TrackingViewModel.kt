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
import java.util.Calendar

class TrackingViewModel (
    private val expenseRecordDao: ExpenseRecordDao, // 주생성자의 매개변수
    private val habitRecordDao: HabitRecordDao,
    private val conditionRecordDao: ConditionRecordDao,
) : ViewModel(){
    var trackingUiState by mutableStateOf(TrackingUiState())
        private set

    private val dateFormat = SimpleDateFormat(
        "yyyy-MM-dd",
        Locale.getDefault()
    )


    init { // 하 기간을 해야하는거군 하 놔
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault() ). format (Date())
        changePeriod(today, today)
        loadTrackingData()
    }

    fun changePeriod(startDate: String, endDate: String){
        trackingUiState = trackingUiState.copy(startDate = startDate, endDate = endDate)
    }

    fun extendPeriodToPast(){
        val startDate = dateFormat.parse(trackingUiState.startDate) ?:return

        val calendar = Calendar.getInstance()
        calendar.time = startDate
        calendar.add(Calendar.DAY_OF_MONTH, -1)

        trackingUiState = trackingUiState.copy(startDate = dateFormat.format(calendar.time))
    }

    fun extendPeriodToFuture(){
        val endDate = dateFormat.parse(trackingUiState.endDate) ?:return

        val calendar = Calendar.getInstance()
        calendar.time = endDate
        calendar.add(Calendar.DAY_OF_MONTH, 1)

        trackingUiState = trackingUiState.copy(endDate = dateFormat.format(calendar.time))
    }

    fun loadTrackingData(){
        viewModelScope.launch{
            val startDate = trackingUiState.startDate
            val endDate = trackingUiState.endDate

            // val conditionTagList = conditionDefinitionDao.getConditionTagList()

            // trackingUiState = trackingUiState.copy(getConditionTagList = conditionTagList)
        }
    }

    // expense
    fun expenseTracking(){
        viewModelScope.launch{
            val start = trackingUiState.startDate
            val end = trackingUiState.endDate

            val tracking = expenseRecordDao.tracking(start, end)
            trackingUiState = trackingUiState.copy(expenseTracking = tracking)
        }
    }

    fun circleGraphingByCategory(categoryId: Long){
        viewModelScope.launch{
            val start = trackingUiState.startDate
            val end = trackingUiState.endDate

            val circleGraph = expenseRecordDao.circleGraphingByCategory(start, end, categoryId)
            trackingUiState = trackingUiState.copy(circleGraphingByCategory = circleGraph)
        }
    }

    fun wholeCircleGraphing(){
        viewModelScope.launch{
            val start = trackingUiState.startDate
            val end = trackingUiState.endDate

            val graph = expenseRecordDao.wholeCircleGraphing(start, end)
            trackingUiState = trackingUiState.copy(wholeCircleGraphing = graph)
        }
    }

    fun calcDailyExpense(){
        viewModelScope.launch{
            val start = trackingUiState.startDate
            val end = trackingUiState.endDate

            val graph = expenseRecordDao.calcDailyExpense(start, end)
            trackingUiState = trackingUiState.copy(calcDailyExpense= graph)
        }
    }

    // habit
    fun habitTrackingByDefinition(){
        viewModelScope.launch{
            val start = trackingUiState.startDate
            val end = trackingUiState.endDate

            val width = habitRecordDao.trackingByDefinition(start, end)
            val length = habitRecordDao.getDefinitionList(start, end)
            trackingUiState = trackingUiState.copy(habitTrackingByDefinition = width, habitDefinitionList = length)
        }
    }

    fun habitTrackingByCategory(){
        viewModelScope.launch {
            val start = trackingUiState.startDate
            val end = trackingUiState.endDate

            val width = habitRecordDao.trackingByCategory(start, end)
            val length = habitRecordDao.getCategoryList(start, end)
            trackingUiState = trackingUiState.copy(habitTrackingByCategory = width, habitCategoryList = length)
        }
    }

    fun monthlyByCategory(categoryId: Long){
        viewModelScope.launch {
            val start = trackingUiState.startDate
            val end = trackingUiState.endDate

            val monthlyData = habitRecordDao.getMonthlyByCategory(categoryId, start, end)
            trackingUiState = trackingUiState.copy(monthlyByCategory = monthlyData)
        }
    }

    // condition
    fun conditionTrackingByDefinition(tags: List<ConditionTag>){
        viewModelScope.launch {
            val start = trackingUiState.startDate
            val end = trackingUiState.endDate

            // val width = conditionRecordDao.trackingByDefinition(start, end)
            val tagIds = tags.map{tag -> tag.id} // 이거 맞나?
            // val length = conditionRecordDao.getDefinitionList(tagIds) // 내가 짠 쿼리에 의문인데...이게 맞나? 아래 태그에 관한것도 세로축을 태그아이디를 넣고 돌리는데 데피니션인데도 태그기반으로 찾는게 맞나? 왜이렇게 했찡?
            // trackingUiState = trackingUiState.copy(conditionTrackingByDefinition = width, conditionDefinitionList = length)
        }
    }

    fun conditionTrackingByTag(tags: List<ConditionTag>){ // getConditionTagList의 결과를 받음
        viewModelScope.launch{
            val start = trackingUiState.startDate
            val end = trackingUiState.endDate

            val tagIds = tags.map{tag -> tag.id}
            // val width = conditionRecordDao.trackingByTag(tagIds, start, end)
            // val length = conditionRecordDao.getTagList(tagIds)
            // trackingUiState = trackingUiState.copy(conditionTrackingByTag = width, conditionTagList = length)
        }
    }

    fun conditionMonthlyByTag(tagId: Long){
        viewModelScope.launch{
            val start = trackingUiState.startDate
            val end = trackingUiState.endDate

            // val trackingData = conditionRecordDao.getMonthlyByTag(tagId, start, end)
            // trackingUiState = trackingUiState.copy(conditionMonthlyByTag = trackingData)
        }
    }
}