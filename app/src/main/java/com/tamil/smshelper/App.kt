package com.tamil.smshelper

import android.app.Application
import com.tamil.smshelper.data.AppDatabase
import com.tamil.smshelper.data.MessageTemplate
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class App : Application() {
    
    override fun onCreate() {
        super.onCreate()
        instance = this
        
        // 初始化默认短信模板
        CoroutineScope(Dispatchers.IO).launch {
            val dao = AppDatabase.getInstance(this@App).messageTemplateDao()
            if (dao.getAllTemplates().isEmpty()) {
                val templates = listOf(
                    "【泰米尔】尊敬的客户，感谢您的支持与信任，祝您生活愉快！",
                    "【会议通知】请于明天上午9点在会议室开会，收到请回复。",
                    "【生日祝福】祝您生日快乐，幸福安康！",
                    "【提醒】您有一个预约尚未确认，请尽快处理。",
                    "【紧急通知】系统维护将于今晚20:00进行，届时暂停服务。",
                    "【活动邀请】诚邀您参加本周六的VIP客户答谢会，期待光临。",
                    "【温馨提示】天气转凉，请及时添衣保暖。",
                    "【发货通知】您的订单已发出，快递单号稍后发送。"
                ).mapIndexed { index, content ->
                    MessageTemplate(content = content, isSelected = index == 0)
                }
                dao.insertAll(templates)
            }
        }
    }

    companion object {
        lateinit var instance: App
            private set
    }
}
