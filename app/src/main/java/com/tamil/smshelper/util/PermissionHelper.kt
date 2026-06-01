package com.tamil.smshelper.util

import android.Manifest
import android.app.Activity
import androidx.core.app.ActivityCompat

object PermissionHelper {
    fun requestSmsPermissions(activity: Activity) {
        ActivityCompat.requestPermissions(
            activity,
            arrayOf(
                Manifest.permission.SEND_SMS,
                Manifest.permission.READ_PHONE_STATE,
                Manifest.permission.READ_CONTACTS
            ),
            1
        )
    }
}
