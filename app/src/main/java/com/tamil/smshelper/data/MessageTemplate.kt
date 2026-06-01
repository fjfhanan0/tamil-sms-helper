package com.tamil.smshelper.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "message_templates")
data class MessageTemplate(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val content: String,
    var isSelected: Boolean = false
)
