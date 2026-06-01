package com.tamil.smshelper.ui

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.tamil.smshelper.R
import com.tamil.smshelper.data.AppDatabase
import com.tamil.smshelper.data.SmsTaskDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class RecordActivity : AppCompatActivity() {
    private lateinit var adapter: StatsAdapter
    private val dao by lazy { AppDatabase.getInstance(this).smsTaskDao() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_record)

        adapter = StatsAdapter { stats ->
            startActivity(Intent(this, RecordDetailActivity::class.java).putExtra("phone", stats.phoneNumber))
        }
        findViewById<RecyclerView>(R.id.recyclerStats).apply {
            layoutManager = LinearLayoutManager(this@RecordActivity)
            adapter = this@RecordActivity.adapter
        }
        findViewById<EditText>(R.id.etSearchRecord).addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) { search(s.toString()) }
            override fun afterTextChanged(s: Editable?) {}
        })
        search("")
    }

    private fun search(keyword: String) {
        lifecycleScope.launch {
            val list = withContext(Dispatchers.IO) {
                if (keyword.isBlank()) dao.getPhoneStatistics() else dao.searchPhones(keyword)
            }
            adapter.submitList(list)
        }
    }
}

class StatsAdapter(private val onClick: (SmsTaskDao.PhoneStats) -> Unit) : RecyclerView.Adapter<StatsAdapter.VH>() {
    private var data: List<SmsTaskDao.PhoneStats> = emptyList()
    fun submitList(list: List<SmsTaskDao.PhoneStats>) { data = list; notifyDataSetChanged() }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_record, parent, false)
        return VH(v)
    }
    override fun onBindViewHolder(holder: VH, position: Int) {
        val stats = data[position]
        holder.tvPhone.text = stats.phoneNumber
        holder.tvSummary.text = "发送${stats.totalCount}次，成功${stats.successCount}，失败${stats.failCount}，回复${stats.replyCount}"
        holder.itemView.setOnClickListener { onClick(stats) }
    }
    override fun getItemCount() = data.size
    class VH(view: View) : RecyclerView.ViewHolder(view) {
        val tvPhone: TextView = view.findViewById(R.id.tvRecordPhone)
        val tvSummary: TextView = view.findViewById(R.id.tvRecordSummary)
    }
}
