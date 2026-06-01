package com.tamil.smshelper.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Telephony
import android.telephony.SmsManager
import com.tamil.smshelper.data.AppDatabase
import com.tamil.smshelper.util.PreferenceHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SmsReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val messages = Telephony.Sms.Intents.getMessagesFromIntent(intent)
        val sender = messages[0].originatingAddress ?: return
        val body = messages[0].messageBody

        // 转发回复
        if (PreferenceHelper.isForwardEnabled(context) && sender.length == 11) {
            val target = PreferenceHelper.getForwardTargetNumber(context) ?: return
            SmsManager.getDefault().sendTextMessage(target, null, "来自 $sender: $body", null, null)
        }

        // 记录回复
        CoroutineScope(Dispatchers.IO).launch {
            val db = AppDatabase.getInstance(context)
            val records = db.smsTaskDao().getRecordsByPhone(sender)
            if (records.isNotEmpty()) {
                val latest = records.first()
                latest.replied = true
                latest.replyContent = body
                db.smsTaskDao().updateRecord(latest)
            }
        }
    }
}
