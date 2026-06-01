package com.tamil.smshelper.ui

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.tamil.smshelper.R
import com.tamil.smshelper.data.AppDatabase
import com.tamil.smshelper.data.SmsTask
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class NumberManageActivity : AppCompatActivity() {
    private lateinit var adapter: NumberAdapter
    private val dao by lazy { AppDatabase.getInstance(this).smsTaskDao() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_number_manage)

        adapter = NumberAdapter { task -> deleteTask(task) }
        val recycler = findViewById<RecyclerView>(R.id.recyclerNumbers)
        recycler.layoutManager = LinearLayoutManager(this)
        recycler.adapter = adapter

        findViewById<Button>(R.id.btnAddNumber).setOnClickListener { showAddDialog() }

        val etSearch = findViewById<EditText>(R.id.etSearch)
        etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) { search(s.toString()) }
            override fun afterTextChanged(s: Editable?) {}
        })
        search("")
    }

    private fun search(keyword: String) {
        lifecycleScope.launch {
            val list = withContext(Dispatchers.IO) {
                if (keyword.isBlank()) dao.getPendingTasks() else dao.searchByPhone(keyword)
            }
            adapter.submitList(list)
        }
    }

    private fun showAddDialog() {
        val view = layoutInflater.inflate(R.layout.dialog_add_number, null)
        val etPhone = view.findViewById<EditText>(R.id.etPhone)
        val etMessage = view.findViewById<EditText>(R.id.etMessage)
        AlertDialog.Builder(this)
            .setTitle("添加号码")
            .setView(view)
            .setPositiveButton("添加") { _, _ ->
                val phone = etPhone.text.toString().trim()
                val msg = etMessage.text.toString().trim()
                if (phone.length == 11 && phone.all { it.isDigit() } && msg.isNotEmpty()) {
                    lifecycleScope.launch {
                        withContext(Dispatchers.IO) {
                            dao.insertTasks(listOf(SmsTask(phoneNumber = phone, message = msg)))
                        }
                        search(findViewById<EditText>(R.id.etSearch).text.toString())
                    }
                } else Toast.makeText(this, "请输入正确的手机号和短信内容", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("取消", null)
            .show()
    }

    private fun deleteTask(task: SmsTask) {
        lifecycleScope.launch {
            withContext(Dispatchers.IO) { dao.deleteTask(task) }
            search(findViewById<EditText>(R.id.etSearch).text.toString())
        }
    }
}

class NumberAdapter(private val onDelete: (SmsTask) -> Unit) : RecyclerView.Adapter<NumberAdapter.VH>() {
    private var data: List<SmsTask> = emptyList()
    fun submitList(list: List<SmsTask>) { data = list; notifyDataSetChanged() }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_number, parent, false)
        return VH(v)
    }
    override fun onBindViewHolder(holder: VH, position: Int) {
        val task = data[position]
        holder.tvPhone.text = task.phoneNumber
        holder.tvMessage.text = task.message
        holder.itemView.setOnLongClickListener { onDelete(task); true }
    }
    override fun getItemCount() = data.size
    class VH(view: View) : RecyclerView.ViewHolder(view) {
        val tvPhone: TextView = view.findViewById(R.id.tvPhone)
        val tvMessage: TextView = view.findViewById(R.id.tvMessage)
    }
}
