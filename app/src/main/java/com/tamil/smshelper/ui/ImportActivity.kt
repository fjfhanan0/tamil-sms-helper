package com.tamil.smshelper.ui

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.tamil.smshelper.R
import com.tamil.smshelper.data.AppDatabase
import com.tamil.smshelper.data.SmsTask
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.apache.poi.ss.usermodel.WorkbookFactory

class ImportActivity : AppCompatActivity() {
    private var phoneCol = 0
    private var msgCol = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_import)

        val etPhoneCol = findViewById<EditText>(R.id.etPhoneCol)
        val etMsgCol = findViewById<EditText>(R.id.etMsgCol)
        findViewById<Button>(R.id.btnImport).setOnClickListener {
            phoneCol = etPhoneCol.text.toString().toIntOrNull() ?: 0
            msgCol = etMsgCol.text.toString().toIntOrNull() ?: 1
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                addCategory(Intent.CATEGORY_OPENABLE)
                type = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet" // xlsx
            }
            startActivityForResult(intent, 400)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 400 && resultCode == RESULT_OK) {
            val uri = data?.data ?: return
            lifecycleScope.launch {
                try {
                    val tasks = withContext(Dispatchers.IO) {
                        contentResolver.openInputStream(uri)?.use { input ->
                            val workbook = WorkbookFactory.create(input)
                            val sheet = workbook.getSheetAt(0)
                            val list = mutableListOf<SmsTask>()
                            for (row in sheet) {
                                val phone = row.getCell(phoneCol)?.stringCellValue ?: continue
                                val msg = row.getCell(msgCol)?.stringCellValue ?: continue
                                if (phone.length >= 11) list.add(SmsTask(phoneNumber = phone, message = msg))
                            }
                            list
                        } ?: emptyList()
                    }
                    if (tasks.isNotEmpty()) {
                        AppDatabase.getInstance(this@ImportActivity).smsTaskDao().insertTasks(tasks)
                        Toast.makeText(this@ImportActivity, "成功导入 ${tasks.size} 条", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(this@ImportActivity, "没有找到有效数据", Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    Toast.makeText(this@ImportActivity, "导入失败: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}
