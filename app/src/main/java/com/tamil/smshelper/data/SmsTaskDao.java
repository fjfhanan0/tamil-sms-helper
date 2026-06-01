package com.tamil.smshelper.data;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface SmsTaskDao {

    @Query("SELECT * FROM sms_task WHERE status = 0 ORDER BY id ASC")
    List<SmsTask> getPendingTasks();

    @Insert
    void insertTasks(SmsTask... tasks);

    @Update
    void updateTask(SmsTask task);

    @Query("UPDATE sms_task SET status = :status WHERE id = :taskId")
    void updateStatus(long taskId, int status);

    @Query("SELECT * FROM sms_task WHERE phoneNumber LIKE '%' || :keyword || '%'")
    List<SmsTask> searchByPhone(String keyword);

    @Delete
    void deleteTask(SmsTask task);

    @Query("DELETE FROM sms_task")
    void clearAll();

    @Insert
    void insertRecord(SendRecord record);

    @Update
    void updateRecord(SendRecord record);

    @Query("SELECT * FROM send_record WHERE phoneNumber = :phone ORDER BY timestamp DESC")
    List<SendRecord> getRecordsByPhone(String phone);

    @Query("SELECT * FROM send_record WHERE phoneNumber LIKE '%' || :keyword || '%' ORDER BY timestamp DESC")
    List<SendRecord> searchPhones(String keyword);

    @Query("SELECT phoneNumber, COUNT(*) as totalCount, " +
            "SUM(CASE WHEN success = 1 THEN 1 ELSE 0 END) as successCount, " +
            "SUM(CASE WHEN success = 0 THEN 1 ELSE 0 END) as failCount, " +
            "SUM(CASE WHEN replied = 1 THEN 1 ELSE 0 END) as replyCount " +
            "FROM send_record GROUP BY phoneNumber")
    List<PhoneStats> getPhoneStatistics();

    class PhoneStats {
        public String phoneNumber;
        public int totalCount;
        public int successCount;
        public int failCount;
        public int replyCount;
    }
}
