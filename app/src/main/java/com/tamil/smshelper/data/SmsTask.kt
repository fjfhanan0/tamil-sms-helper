package com.tamil.smshelper.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "sms_tasks")
data class SmsTask(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val phoneNumber: String,
    val message: String,
    val status: Int = 0
)
