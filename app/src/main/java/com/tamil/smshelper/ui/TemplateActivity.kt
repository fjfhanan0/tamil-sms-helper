package com.tamil.smshelper.ui

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.RadioButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.tamil.smshelper.R
import com.tamil.smshelper.data.AppDatabase
import com.tamil.smshelper.data.MessageTemplate
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class TemplateActivity : AppCompatActivity() {
    private lateinit var adapter: TemplateAdapter
    private var selectedContent: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_template)

        val recycler = findViewById<RecyclerView>(R.id.recyclerTemplates)
        recycler.layoutManager = LinearLayoutManager(this)
        adapter = TemplateAdapter { content -> selectedContent = content }
        recycler.adapter = adapter

        lifecycleScope.launch {
            val templates = withContext(Dispatchers.IO) {
                AppDatabase.getInstance(this@TemplateActivity).messageTemplateDao().getAllTemplates()
            }
            adapter.submitList(templates)
        }

        findViewById<Button>(R.id.btnConfirm).setOnClickListener {
            if (selectedContent != null) {
                setResult(RESULT_OK, Intent().putExtra("selected_message", selectedContent))
                finish()
            } else {
                Toast.makeText(this, "请选择一条短信模板", Toast.LENGTH_SHORT).show()
            }
        }
    }

    class TemplateAdapter(private val onSelected: (String) -> Unit) : RecyclerView.Adapter<TemplateAdapter.VH>() {
        private var data: List<MessageTemplate> = emptyList()
        fun submitList(list: List<MessageTemplate>) { data = list; notifyDataSetChanged() }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
            val v = LayoutInflater.from(parent.context).inflate(R.layout.item_template, parent, false)
            return VH(v)
        }
        override fun onBindViewHolder(holder: VH, position: Int) {
            val template = data[position]
            holder.content.text = template.content
            holder.radio.isChecked = template.isSelected
            holder.itemView.setOnClickListener {
                onSelected(template.content)
                data.forEach { it.isSelected = false }
                template.isSelected = true
                notifyDataSetChanged()
            }
        }
        override fun getItemCount() = data.size
        class VH(view: View) : RecyclerView.ViewHolder(view) {
            val content: TextView = view.findViewById(R.id.tvTemplateContent)
            val radio: RadioButton = view.findViewById(R.id.rbTemplate)
        }
    }
}
