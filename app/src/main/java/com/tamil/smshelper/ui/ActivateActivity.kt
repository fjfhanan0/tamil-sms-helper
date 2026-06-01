package com.tamil.smshelper.ui

import android.content.ClipData
import android.content.ClipboardManager
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.tamil.smshelper.R
import com.tamil.smshelper.util.LicenseHelper

class ActivateActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_activate)

        val tvDeviceId = findViewById<TextView>(R.id.tvDeviceId)
        val etLimit = findViewById<EditText>(R.id.etLimit)
        val etCode = findViewById<EditText>(R.id.etCode)
        val btnActivate = findViewById<Button>(R.id.btnActivate)

        val deviceId = LicenseHelper.getDeviceId(this)
        tvDeviceId.text = "设备码：$deviceId (点击复制)"
        tvDeviceId.setOnClickListener {
            val clipboard = getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
            clipboard.setPrimaryClip(ClipData.newPlainText("device_id", deviceId))
            Toast.makeText(this, "已复制", Toast.LENGTH_SHORT).show()
        }

        btnActivate.setOnClickListener {
            val limit = etLimit.text.toString().toIntOrNull()
            val code = etCode.text.toString().trim()
            if (limit == null || limit <= 0) {
                Toast.makeText(this, "请输入有效条数", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (LicenseHelper.verifyActivationCode(this, code, limit)) {
                LicenseHelper.activate(this, limit)
                Toast.makeText(this, "激活成功！额度：$limit 条", Toast.LENGTH_LONG).show()
                finish()
            } else {
                Toast.makeText(this, "激活码无效", Toast.LENGTH_LONG).show()
            }
        }
    }
}
