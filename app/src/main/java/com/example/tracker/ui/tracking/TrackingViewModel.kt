package com.example.tracker.ui.tracking

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.example.tracker.data.dao.ConditionRecordDao
import com.example.tracker.data.dao.ExpenseRecordDao
import com.example.tracker.data.dao.HabitRecordDao

class TrackingViewModel (
    private val expenseRecordDao: ExpenseRecordDao, // 주생성자의 매개변수
    private val habitRecordDao: HabitRecordDao,
    private val conditionRecordDao: ConditionRecordDao
) : ViewModel(){
    val trackingUiState by mutableStateOf(TrackingUiState())
}