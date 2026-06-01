package com.tamil.smshelper.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.telephony.SmsManager
import android.telephony.TelephonyManager
import com.tamil.smshelper.service.SmsSenderService
import com.tamil.smshelper.util.PreferenceHelper

class PhoneStateReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (!PreferenceHelper.isAutoReplyEnabled(context)) return

        val state = intent.getStringExtra(TelephonyManager.EXTRA_STATE)
        val incomingNumber = intent.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER)

        if (state == TelephonyManager.EXTRA_STATE_RINGING && incomingNumber?.length == 11) {
            context.sendBroadcast(Intent(SmsSenderService.ACTION_PAUSE).setPackage(context.packageName))
            val message = PreferenceHelper.getAutoReplyMessage(context) ?: "我正在忙，稍后联系您。"
            SmsManager.getDefault().sendTextMessage(incomingNumber, null, message, null, null)
        } else if (state == TelephonyManager.EXTRA_STATE_IDLE) {
            context.sendBroadcast(Intent(SmsSenderService.ACTION_RESUME).setPackage(context.packageName))
        }
    }
}
