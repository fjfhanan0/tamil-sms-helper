package com.tamil.smshelper.data;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface MessageTemplateDao {

    @Query("SELECT * FROM message_template ORDER BY id ASC")
    List<MessageTemplate> getAllTemplates();

    @Insert
    void insertAll(MessageTemplate... templates);

    @Update
    void updateTemplate(MessageTemplate template);

    @Query("SELECT * FROM message_template WHERE isSelected = 1 LIMIT 1")
    MessageTemplate getSelectedTemplate();

    @Query("UPDATE message_template SET isSelected = 0")
    void clearAllSelections();
}
