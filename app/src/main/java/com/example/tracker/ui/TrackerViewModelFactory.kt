package com.example.tracker.ui

import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModel
import com.example.tracker.data.database.AppDatabase
import com.example.tracker.ui.daily.DailyViewModel
import com.example.tracker.ui.tracking.TrackingViewModel

class TrackerViewModelFactory(private val db: AppDatabase): ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T: ViewModel> create(
    modelClass : Class<T>
    ): T{
        return when{
            modelClass.isAssignableFrom(DailyViewModel::class.java) ->
            {
                DailyViewModel(
                    expenseRecordDao = db.expenseRecordDao(),
                    expenseSubCategoryDao = db.expenseSubCategoryDao(),
                    habitRecordDao = db.habitRecordDao(),
                    conditionRecordDao = db.conditionRecordDao(),
                    itemDefinitionDao = db.itemDefinitionDao(),
                    habitDefinitionDao = db.habitDefinitionDao(),
                    habitCategoryDefinitionDao = db.habitCategoryDefinitionDao(),
                ) as T
            }

            modelClass.isAssignableFrom(TrackingViewModel::class.java) ->
            {
                TrackingViewModel(
                    expenseRecordDao = db.expenseRecordDao(),
                    habitRecordDao = db.habitRecordDao(),
                    conditionRecordDao = db.conditionRecordDao()
                ) as T
            }

            else -> {
                throw IllegalArgumentException("알수없는 viewModel: ${modelClass.name}")
            }
        }
    }
}
