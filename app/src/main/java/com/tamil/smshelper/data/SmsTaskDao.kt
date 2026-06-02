package com.tamil.smshelper.data

import androidx.room.*

@Dao
interface SmsTaskDao {

    @Query("SELECT * FROM sms_tasks WHERE status = 0 ORDER BY id ASC")
    fun getPendingTasks(): List<SmsTask>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertTasks(tasks: List<SmsTask>)

    @Update
    fun updateTask(task: SmsTask)

    @Query("UPDATE sms_tasks SET status = :status WHERE id = :taskId")
    fun updateStatus(taskId: Long, status: Int)

    @Query("DELETE FROM sms_tasks")
    fun clearAll()

    @Insert
    fun insertRecord(record: SendRecord)

    @Update
    fun updateRecord(record: SendRecord)

    @Query("SELECT * FROM sms_tasks WHERE phoneNumber LIKE '%' || :keyword || '%' ORDER BY id ASC")
    fun searchByPhone(keyword: String): List<SmsTask>

    @Delete
    fun deleteTask(task: SmsTask)

    @Query("SELECT * FROM send_records WHERE phoneNumber = :phone ORDER BY timestamp DESC")
    fun getRecordsByPhone(phone: String): List<SendRecord>

    @Query("SELECT phoneNumber, COUNT(*) AS totalCount, " +
            "SUM(CASE WHEN success THEN 1 ELSE 0 END) AS successCount, " +
            "SUM(CASE WHEN success THEN 0 ELSE 1 END) AS failCount, " +
            "SUM(CASE WHEN replied THEN 1 ELSE 0 END) AS replyCount " +
            "FROM send_records GROUP BY phoneNumber ORDER BY phoneNumber")
    fun getPhoneStatistics(): List<PhoneStats>

    @Query("SELECT phoneNumber, COUNT(*) AS totalCount, 0 AS successCount, 0 AS failCount, 0 AS replyCount FROM send_records WHERE phoneNumber LIKE '%' || :keyword || '%' GROUP BY phoneNumber")
    fun searchPhones(keyword: String): List<PhoneStats>

    data class PhoneStats(
        val phoneNumber: String,
        val totalCount: Int,
        val successCount: Int,
        val failCount: Int,
        val replyCount: Int
    )
}
