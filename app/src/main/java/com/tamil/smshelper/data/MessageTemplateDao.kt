package com.tamil.smshelper.data

import androidx.room.*

@Dao
interface MessageTemplateDao {

    @Query("SELECT * FROM message_templates ORDER BY id ASC")
    fun getAllTemplates(): List<MessageTemplate>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(templates: List<MessageTemplate>)

    @Update
    fun updateTemplate(template: MessageTemplate)

    @Query("UPDATE message_templates SET isSelected = 0")
    fun clearAllSelections()

    @Query("SELECT * FROM message_templates WHERE isSelected = 1 LIMIT 1")
    fun getSelectedTemplate(): MessageTemplate?
}
