package com.tamil.smshelper.util

import android.content.Context

object PreferenceHelper {
    private const val PREFS_NAME = "sms_helper_prefs"

    private fun getPrefs(context: Context) = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    var sendInterval: Long
        get() = getPrefs(context).getLong("send_interval", 1000)
        set(value) { getPrefs(context).edit().putLong("send_interval", value).apply() }

    var simSlot: Int
        get() = getPrefs(context).getInt("sim_slot", -1)
        set(value) { getPrefs(context).edit().putInt("sim_slot", value).apply() }

    var timeLimitEnabled: Boolean
        get() = getPrefs(context).getBoolean("time_limit_enabled", true)
        set(value) { getPrefs(context).edit().putBoolean("time_limit_enabled", value).apply() }

    var autoReplyEnabled: Boolean
        get() = getPrefs(context).getBoolean("auto_reply_enabled", false)
        set(value) { getPrefs(context).edit().putBoolean("auto_reply_enabled", value).apply() }

    var autoReplyMessage: String?
        get() = getPrefs(context).getString("auto_reply_message", "我正在忙，稍后联系您。")
        set(value) { getPrefs(context).edit().putString("auto_reply_message", value).apply() }

    var forwardEnabled: Boolean
        get() = getPrefs(context).getBoolean("forward_enabled", false)
        set(value) { getPrefs(context).edit().putBoolean("forward_enabled", value).apply() }

    var forwardTarget: String?
        get() = getPrefs(context).getString("forward_target", null)
        set(value) { getPrefs(context).edit().putString("forward_target", value).apply() }

    // 兼容之前的函数式调用
    fun getInterval(context: Context): Long = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).getLong("send_interval", 1000)
    fun getSimSlot(context: Context): Int = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).getInt("sim_slot", -1)
    fun setSimSlot(context: Context, slot: Int) = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit().putInt("sim_slot", slot).apply()
    fun isTimeLimitEnabled(context: Context): Boolean = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).getBoolean("time_limit_enabled", true)
    fun isAutoReplyEnabled(context: Context): Boolean = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).getBoolean("auto_reply_enabled", false)
    fun getAutoReplyMessage(context: Context): String? = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).getString("auto_reply_message", "我正在忙，稍后联系您。")
    fun isForwardEnabled(context: Context): Boolean = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).getBoolean("forward_enabled", false)
    fun getForwardTargetNumber(context: Context): String? = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).getString("forward_target", null)
}
