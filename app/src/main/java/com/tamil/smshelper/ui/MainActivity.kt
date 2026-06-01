package com.tamil.smshelper.ui

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.tamil.smshelper.R
import com.tamil.smshelper.data.AppDatabase
import com.tamil.smshelper.databinding.ActivityMainBinding
import com.tamil.smshelper.service.FloatingBallService
import com.tamil.smshelper.service.SmsSenderService
import com.tamil.smshelper.util.LicenseHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private var contactsSelectedNumbers: List<String>? = null

    private val keyReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            Toast.makeText(this@MainActivity, "需要激活或购买更多条数", Toast.LENGTH_LONG).show()
            startActivity(Intent(this@MainActivity, ActivateActivity::class.java))
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        registerReceiver(keyReceiver, IntentFilter("com.tamil.smshelper.KEY_REQUIRED"))
        updateRemaining()

        binding.btnImport.setOnClickListener { startActivity(Intent(this, ImportActivity::class.java)) }
        binding.btnPickContacts.setOnClickListener {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED)
                startActivityForResult(Intent(this, ContactsPickerActivity::class.java), 200)
            else
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_CONTACTS), 100)
        }
        binding.btnNumberManage.setOnClickListener { startActivity(Intent(this, NumberManageActivity::class.java)) }
        binding.btnRecord.setOnClickListener { startActivity(Intent(this, RecordActivity::class.java)) }
        binding.btnActivate.setOnClickListener { startActivity(Intent(this, ActivateActivity::class.java)) }

        binding.btnStartSend.setOnClickListener {
            if (!LicenseHelper.isActivated(this)) {
                Toast.makeText(this, "请先激活App", Toast.LENGTH_SHORT).show()
                startActivity(Intent(this, ActivateActivity::class.java))
                return@setOnClickListener
            }
            if (LicenseHelper.getRemaining(this) <= 0) {
                Toast.makeText(this, "发送条数已用完，请购买更多", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            startService(Intent(this, SmsSenderService::class.java))
            if (Settings.canDrawOverlays(this)) startService(Intent(this, FloatingBallService::class.java))
            else startActivity(Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION))
        }
        binding.btnStopSend.setOnClickListener {
            stopService(Intent(this, SmsSenderService::class.java))
            stopService(Intent(this, FloatingBallService::class.java))
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 100 && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)
            startActivityForResult(Intent(this, ContactsPickerActivity::class.java), 200)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 200 && resultCode == RESULT_OK) {
            contactsSelectedNumbers = data?.getStringArrayListExtra("selected_numbers")
            startActivityForResult(Intent(this, TemplateActivity::class.java), 300)
        } else if (requestCode == 300 && resultCode == RESULT_OK) {
            val message = data?.getStringExtra("selected_message") ?: return
            val numbers = contactsSelectedNumbers ?: return
            lifecycleScope.launch {
                withContext(Dispatchers.IO) {
                    val dao = AppDatabase.getInstance(this@MainActivity).smsTaskDao()
                    dao.insertTasks(numbers.map { com.tamil.smshelper.data.SmsTask(phoneNumber = it, message = message) })
                }
                Toast.makeText(this@MainActivity, "已添加 ${numbers.size} 个号码", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        updateRemaining()
    }

    private fun updateRemaining() {
        binding.tvRemaining.text = if (LicenseHelper.isActivated(this)) "剩余：${LicenseHelper.getRemaining(this)} 条" else "未激活"
    }

    override fun onDestroy() {
        unregisterReceiver(keyReceiver)
        super.onDestroy()
    }
}
