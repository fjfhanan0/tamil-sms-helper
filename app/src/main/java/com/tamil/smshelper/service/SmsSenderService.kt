package com.tamil.smshelper.service

import android.app.*
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.IBinder
import android.telephony.SubscriptionManager
import android.telephony.SmsManager
import androidx.core.app.NotificationCompat
import com.tamil.smshelper.data.AppDatabase
import com.tamil.smshelper.data.SendRecord
import com.tamil.smshelper.util.LicenseHelper
import com.tamil.smshelper.util.PreferenceHelper
import kotlinx.coroutines.*
import java.util.*

class SmsSenderService : Service() {
    private lateinit var db: AppDatabase
    private lateinit var dao: com.tamil.smshelper.data.SmsTaskDao
    private val scope = CoroutineScope(Dispatchers.IO)
    private var isPaused = false
    private var isRunning = true
    private var sendInterval: Long = 1000
    private var useSimSlot: Int = -1  // -1=默认，0=卡1，1=卡2
    private var timeLimitEnabled = true
    private var consecutiveFailures = 0  // 连续失败计数

    companion object {
        const val ACTION_PAUSE = "com.tamil.smshelper.PAUSE"
        const val ACTION_RESUME = "com.tamil.smshelper.RESUME"
        const val ACTION_STOP = "com.tamil.smshelper.STOP"
    }

    override fun onCreate() {
        super.onCreate()
        db = AppDatabase.getInstance(this)
        dao = db.smsTaskDao()
        createNotificationChannel()
        startForeground(1, createNotification("准备就绪"))
        sendInterval = PreferenceHelper.getInterval(this)
        useSimSlot = PreferenceHelper.getSimSlot(this)
        timeLimitEnabled = PreferenceHelper.isTimeLimitEnabled(this)
        registerReceiver(resumeReceiver, IntentFilter(ACTION_RESUME))
        startSendingLoop()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_PAUSE -> isPaused = true
            ACTION_RESUME -> {
                isPaused = false
                if (!isRunning) {
                    isRunning = true
                    startSendingLoop()
                }
            }
            ACTION_STOP -> stopSelf()
        }
        return START_STICKY
    }

    private val resumeReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            isPaused = false
            if (!isRunning) {
                isRunning = true
                startSendingLoop()
            }
        }
    }

    private fun startSendingLoop() {
        scope.launch {
            while (isRunning) {
                if (isPaused || !isTimeInWindow()) {
                    delay(1000)
                    continue
                }

                // 激活检查
                if (!LicenseHelper.isActivated(this@SmsSenderService) || LicenseHelper.getRemaining(this@SmsSenderService) <= 0) {
                    isPaused = true
                    sendBroadcast(Intent("com.tamil.smshelper.KEY_REQUIRED"))
                    delay(1000)
                    continue
                }

                val tasks = dao.getPendingTasks()
                if (tasks.isEmpty()) {
                    isRunning = false
                    updateNotification("发送完毕")
                    break
                }

                val task = tasks.first()
                try {
                    sendSms(task.phoneNumber, task.message)
                    dao.updateStatus(task.id, 1)
                    dao.insertRecord(SendRecord(phoneNumber = task.phoneNumber, success = true))
                    LicenseHelper.incrementSent(this@SmsSenderService)
                    consecutiveFailures = 0  // 成功清零
                    sendBroadcast(Intent("com.tamil.smshelper.SMS_SENT_SUCCESS"))
                } catch (e: Exception) {
                    dao.updateStatus(task.id, 2)
                    dao.insertRecord(SendRecord(phoneNumber = task.phoneNumber, success = false))
                    consecutiveFailures++
                    // 连续失败 5 次，尝试切换 SIM 卡
                    if (consecutiveFailures >= 5) {
                        if (switchToOtherSim()) {
                            consecutiveFailures = 0
                            updateNotification("已自动切换SIM卡")
                        } else {
                            updateNotification("发送失败过多，可能已封卡，请检查")
                            isPaused = true
                            sendBroadcast(Intent("com.tamil.smshelper.SIM_BLOCKED"))
                            delay(5000)
                            continue
                        }
                    }
                }
                updateNotification("发送中... 剩余: ${LicenseHelper.getRemaining(this@SmsSenderService)}")
                delay(sendInterval)
            }
            stopSelf()
        }
    }

    private fun sendSms(phone: String, msg: String) {
        val smsManager = if (useSimSlot >= 0) {
            val subInfo = SubscriptionManager.from(this).activeSubscriptionInfoList
            val subId = subInfo?.find { it.simSlotIndex == useSimSlot }?.subscriptionId
            if (subId != null) SmsManager.getSmsManagerForSubscriptionId(subId)
            else SmsManager.getDefault()
        } else {
            SmsManager.getDefault()
        }
        smsManager.sendTextMessage(phone, null, msg, null, null)
    }

    private fun switchToOtherSim(): Boolean {
        val sims = SubscriptionManager.from(this).activeSubscriptionInfoList ?: return false
        if (sims.size < 2) return false  // 单卡无法切换
        // 如果当前用卡1，切到卡2；用卡2或默认，切到卡1
        val newSlot = if (useSimSlot == 0) 1 else 0
        if (sims.any { it.simSlotIndex == newSlot }) {
            useSimSlot = newSlot
            PreferenceHelper.setSimSlot(this, newSlot)
            return true
        }
        return false
    }

    private fun isTimeInWindow(): Boolean {
        if (!timeLimitEnabled) return true
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        return hour in 8..19  // 20:00 停止
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel("sms_channel", "短信发送", NotificationManager.IMPORTANCE_LOW)
        getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
    }

    private fun createNotification(text: String) = NotificationCompat.Builder(this, "sms_channel")
        .setContentTitle("泰米尔短信助手")
        .setContentText(text)
        .setSmallIcon(android.R.drawable.ic_dialog_info)
        .setPriority(NotificationCompat.PRIORITY_LOW)
        .build()

    private fun updateNotification(text: String) {
        getSystemService(NotificationManager::class.java).notify(1, createNotification(text))
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        isRunning = false
        scope.cancel()
        unregisterReceiver(resumeReceiver)
        super.onDestroy()
    }
}
