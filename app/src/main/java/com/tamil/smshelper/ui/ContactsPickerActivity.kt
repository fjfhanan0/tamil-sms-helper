package com.tamil.smshelper.ui

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.database.Cursor
import android.os.Bundle
import android.provider.ContactsContract
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.tamil.smshelper.R

class ContactsPickerActivity : AppCompatActivity() {
    private lateinit var adapter: ContactAdapter
    private lateinit var tvCount: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_contacts_picker)

        tvCount = findViewById(R.id.tvSelectedCount)
        val recycler = findViewById<RecyclerView>(R.id.recyclerContacts)

        val contacts = loadContacts()
        adapter = ContactAdapter(contacts)
        adapter.onSelectionChanged = { tvCount.text = "已选择: ${adapter.getSelectedCount()} 人" }
        recycler.layoutManager = LinearLayoutManager(this)
        recycler.adapter = adapter

        findViewById<Button>(R.id.btnSelectAll).setOnClickListener {
            adapter.selectAll()
            tvCount.text = "已选择: ${adapter.getSelectedCount()} 人"
        }
        findViewById<Button>(R.id.btnDeselectAll).setOnClickListener {
            adapter.deselectAll()
            tvCount.text = "已选择: ${adapter.getSelectedCount()} 人"
        }
        findViewById<Button>(R.id.btnDone).setOnClickListener {
            val selected = adapter.getSelectedNumbers()
            if (selected.isEmpty()) {
                Toast.makeText(this, "请至少选择一个联系人", Toast.LENGTH_SHORT).show()
            } else {
                val result = Intent().apply { putStringArrayListExtra("selected_numbers", ArrayList(selected)) }
                setResult(RESULT_OK, result)
                finish()
            }
        }
    }

    private fun loadContacts(): List<ContactAdapter.Contact> {
        val list = mutableListOf<ContactAdapter.Contact>()
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_CONTACTS), 999)
            return list
        }
        val uri = ContactsContract.CommonDataKinds.Phone.CONTENT_URI
        val cursor: Cursor? = contentResolver.query(uri, arrayOf(
            ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
            ContactsContract.CommonDataKinds.Phone.NUMBER
        ), null, null, null)
        cursor?.use {
            val nameIdx = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)
            val numIdx = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
            while (it.moveToNext()) {
                val name = it.getString(nameIdx) ?: "未知"
                val phone = it.getString(numIdx)?.replace(Regex("[^+0-9]"), "") ?: ""
                if (phone.length >= 11) list.add(ContactAdapter.Contact(name, phone))
            }
        }
        return list.distinctBy { it.phone }
    }
}
