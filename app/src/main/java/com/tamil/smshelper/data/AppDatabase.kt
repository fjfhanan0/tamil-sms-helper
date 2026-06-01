package com.tamil.smshelper.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [SmsTask::class, SendRecord::class, MessageTemplate::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun smsTaskDao(): SmsTaskDao
    abstract fun messageTemplateDao(): MessageTemplateDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "tamil_sms_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
