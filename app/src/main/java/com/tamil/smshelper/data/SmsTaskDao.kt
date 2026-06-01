package com.tamil.smshelper.data

import androidx.room.*

@Dao
interface SmsTaskDao {
    @Query("SELECT * FROM sms_tasks WHERE status = 0 ORDER BY id ASC")
    suspend fun getPendingTasks(): List<SmsTask>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTasks(tasks: List<SmsTask>)

    @Update
    suspend fun updateTask(task: SmsTask)

    @Query("UPDATE sms_tasks SET status = :status WHERE id = :taskId")
    suspend fun updateStatus(taskId: Long, status: Int)

    @Query("DELETE FROM sms_tasks")
    suspend fun clearAll()

    @Insert
    suspend fun insertRecord(record: SendRecord)

    @Update
    suspend fun updateRecord(record: SendRecord)

    @Query("SELECT * FROM sms_tasks WHERE phoneNumber LIKE '%' || :keyword || '%' ORDER BY id ASC")
    suspend fun searchByPhone(keyword: String): List<SmsTask>

    @Delete
    suspend fun deleteTask(task: SmsTask)

    @Query("""
        SELECT phoneNumber,
               COUNT(*) AS totalCount,
               SUM(CASE WHEN success THEN 1 ELSE 0 END) AS successCount,
               SUM(CASE WHEN success THEN 0 ELSE 1 END) AS failCount,
               SUM(CASE WHEN replied THEN 1 ELSE 0 END) AS replyCount
        FROM send_records
        GROUP BY phoneNumber
        ORDER BY phoneNumber
    """)
    suspend fun getPhoneStatistics(): List<PhoneStats>

    @Query("SELECT * FROM send_records WHERE phoneNumber LIKE '%' || :keyword || '%' GROUP BY phoneNumber")
    suspend fun searchPhones(keyword: String): List<PhoneStats>

    @Query("SELECT * FROM send_records WHERE phoneNumber = :phone ORDER BY timestamp DESC")
    suspend fun getRecordsByPhone(phone: String): List<SendRecord>

    data class PhoneStats(
        val phoneNumber: String,
        val totalCount: Int,
        val successCount: Int,
        val failCount: Int,
        val replyCount: Int
    )
}
