package com.example.tracker.data.database

import android.content.Context
import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

object DatabaseProvider{
    @Volatile
    private var INSTANCE: AppDatabase? = null
    private val MIGRATION_1_2 = object: Migration(1,2){
        override fun migrate(db: SupportSQLiteDatabase){
            db.execSQL(
                """
                DROP INDEX IF EXISTS
                `index_expense_record_date_itemId_subCategoryId`
                """.trimIndent()
            )

            db.execSQL(
                """
                CREATE UNIQUE INDEX IF NOT EXISTS
                `index_expense_record_date_itemId_subCategoryId_unitPrice`
                ON `expense_record`
                (`date`, `itemId`, `subCategoryId`, `unitPrice`)
                """.trimIndent()
            )
        }
    }

    fun getDatabase(context: Context): AppDatabase{
        return INSTANCE ?: synchronized(this){ //null이면 후자? 아니면 그대로 반환?
            // 근데 synchronized(this){~~는 대체 뭐야
            val instance = Room.databaseBuilder(
                context.applicationContext,
                AppDatabase::class.java, // 이 문법은 이해를 못하겠군
                "tracker_db"
            )
                .addMigrations(MIGRATION_1_2)
                .build()
            INSTANCE = instance
            instance
        }
    }
}// 앱에서 DB객체 꺼내 쓰는 도구? 이건 또 왜 있어? 어렵당...
// 방금꺼는 그니까 dao라던가 등등은 앱에서 DB객체를 꺼내 쓰는 도구가 아니란 말이야? 무슨차이지?
