package com.tamil.smshelper.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.tamil.smshelper.R
import com.tamil.smshelper.data.AppDatabase
import com.tamil.smshelper.data.SendRecord
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

class RecordDetailActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_record_detail)
        val phone = intent.getStringExtra("phone") ?: return
        title = "发送记录 - $phone"

        val adapter = DetailAdapter()
        findViewById<RecyclerView>(R.id.recyclerDetail).apply {
            layoutManager = LinearLayoutManager(this@RecordDetailActivity)
            this.adapter = adapter
        }
        lifecycleScope.launch {
            val records = withContext(Dispatchers.IO) {
                AppDatabase.getInstance(this@RecordDetailActivity).smsTaskDao().getRecordsByPhone(phone)
            }
            adapter.submitList(records)
        }
    }
}

class DetailAdapter : RecyclerView.Adapter<DetailAdapter.VH>() {
    private var data: List<SendRecord> = emptyList()
    fun submitList(list: List<SendRecord>) { data = list; notifyDataSetChanged() }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_send_detail, parent, false)
        return VH(v)
    }
    override fun onBindViewHolder(holder: VH, position: Int) {
        val record = data[position]
        holder.tvTime.text = SimpleDateFormat("MM-dd HH:mm:ss", Locale.getDefault()).format(Date(record.timestamp))
        holder.tvStatus.text = if (record.success) "成功" else "失败"
        holder.tvReply.text = if (record.replied) "回复:${record.replyContent}" else "未回复"
    }
    override fun getItemCount() = data.size
    class VH(view: View) : RecyclerView.ViewHolder(view) {
        val tvTime: TextView = view.findViewById(R.id.tvSendTime)
        val tvStatus: TextView = view.findViewById(R.id.tvSendStatus)
        val tvReply: TextView = view.findViewById(R.id.tvReplyInfo)
    }
}
