package com.example.tracker.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.tracker.data.dao.ConditionDefinitionDao
import com.example.tracker.data.dao.ConditionRecordDao
import com.example.tracker.data.dao.DailyEntryDao
import com.example.tracker.data.dao.ExpenseRecordDao
import com.example.tracker.data.dao.ExpenseSubCategoryDao
import com.example.tracker.data.dao.HabitCategoryDefinitionDao
import com.example.tracker.data.dao.HabitDefinitionDao
import com.example.tracker.data.dao.HabitRecordDao
import com.example.tracker.data.dao.ItemDefinitionDao
import com.example.tracker.data.entity.ConditionCategory
import com.example.tracker.data.entity.ConditionDefinition
import com.example.tracker.data.entity.ConditionCheckRecord
import com.example.tracker.data.entity.ConditionDefinitionTag
import com.example.tracker.data.entity.ConditionTag
import com.example.tracker.data.entity.DailyEntry
import com.example.tracker.data.entity.ExpenseRecord
import com.example.tracker.data.entity.ExpenseSubCategoryDefinition
import com.example.tracker.data.entity.HabitCategoryDefinition
import com.example.tracker.data.entity.HabitDefinition
import com.example.tracker.data.entity.HabitRecord
import com.example.tracker.data.entity.ItemDefinition

@Database(// 되게 신기한게 클래스로 시작되지가 않는다 이건? 왜지? 이 어노테이션은 뭐지
    entities = [
        ExpenseSubCategoryDefinition::class,
        ExpenseRecord::class,
        ItemDefinition::class,
        HabitDefinition::class,
        ConditionDefinition::class,
        HabitRecord::class,
        ConditionCheckRecord::class,
        ConditionCategory::class,
        DailyEntry::class,
        ConditionDefinitionTag::class,
        ConditionTag::class,
        HabitCategoryDefinition::class,
    ],
    version = 1 //처음보는 문법임
)

abstract class AppDatabase : RoomDatabase() { // 인터페이스랑의 차이가 뭐지?
    abstract fun habitRecordDao(): HabitRecordDao
    abstract fun habitDefinitionDao(): HabitDefinitionDao
    abstract fun conditionDefinitionDao(): ConditionDefinitionDao
    abstract fun conditionRecordDao(): ConditionRecordDao
    abstract fun dailyEntryDao(): DailyEntryDao
    abstract fun expenseRecordDao(): ExpenseRecordDao
    abstract fun expenseSubCategoryDao(): ExpenseSubCategoryDao
    abstract fun itemDefinitionDao(): ItemDefinitionDao
    abstract fun habitCategoryDefinitionDao(): HabitCategoryDefinitionDao
}
// 우리 앱 DB 전체? 이걸 하는 이유가 뭔진 잘 모르겠당ㅎㅎ
// 왜 추상으로 할까 인터페이스가 아니라. 추상과 인터페이스의 차이...추상은 덜구현된거랑 다 구현된거랑 섞여있는데 인터페이스는 그렇진 않았던듯