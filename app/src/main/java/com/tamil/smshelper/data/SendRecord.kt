package com.tamil.smshelper.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "send_records")
data class SendRecord(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val phoneNumber: String,
    val timestamp: Long = System.currentTimeMillis(),
    val success: Boolean,
    var replied: Boolean = false,
    var replyContent: String? = null
)
