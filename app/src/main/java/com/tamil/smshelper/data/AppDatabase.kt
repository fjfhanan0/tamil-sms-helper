package com.tamil.smshelper.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [SmsTask::class, SendRecord::class, MessageTemplate::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun smsTaskDao(): SmsTaskDao
    abstract fun messageTemplateDao(): MessageTemplateDao

    companion object {
        @Volatile private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                Room.databaseBuilder(context, AppDatabase::class.java, "sms_helper_db")
                    .fallbackToDestructiveMigration()
                    .build().also { INSTANCE = it }
            }
        }
    }
}
