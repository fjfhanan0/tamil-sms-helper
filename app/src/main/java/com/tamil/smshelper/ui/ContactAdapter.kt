package com.tamil.smshelper.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.tamil.smshelper.R

class ContactAdapter(private var contacts: List<Contact>) : RecyclerView.Adapter<ContactAdapter.VH>() {

    data class Contact(val name: String, val phone: String)

    private val selectedSet = mutableSetOf<String>()
    var onSelectionChanged: (() -> Unit)? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_contact, parent, false)
        return VH(view)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val contact = contacts[position]
        holder.tvName.text = contact.name
        holder.tvPhone.text = contact.phone
        holder.checkBox.isChecked = selectedSet.contains(contact.phone)
        holder.itemView.setOnClickListener {
            if (selectedSet.contains(contact.phone)) selectedSet.remove(contact.phone) else selectedSet.add(contact.phone)
            notifyItemChanged(position)
            onSelectionChanged?.invoke()
        }
    }

    override fun getItemCount() = contacts.size

    fun getSelectedNumbers(): List<String> = selectedSet.toList()
    fun getSelectedCount() = selectedSet.size

    fun selectAll() {
        contacts.forEach { selectedSet.add(it.phone) }
        notifyDataSetChanged()
        onSelectionChanged?.invoke()
    }

    fun deselectAll() {
        selectedSet.clear()
        notifyDataSetChanged()
        onSelectionChanged?.invoke()
    }

    class VH(view: View) : RecyclerView.ViewHolder(view) {
        val tvName: TextView = view.findViewById(R.id.tvContactName)
        val tvPhone: TextView = view.findViewById(R.id.tvContactPhone)
        val checkBox: CheckBox = view.findViewById(R.id.checkbox)
    }
}
