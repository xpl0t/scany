package com.xpl0t.scany.ui.scanlist

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.xpl0t.scany.R
import com.xpl0t.scany.models.Scan

class ScanItemAdapter : RecyclerView.Adapter<ScanItemAdapter.ViewHolder>() {

    private var items: List<Scan> = listOf()

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val title: TextView = view.findViewById(R.id.title)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.scan_list_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]

        holder.title.text = item.name
    }

    override fun getItemCount(): Int = items.count()

    fun updateItems(items: List<Scan>) {
        this.items = items
        notifyDataSetChanged()
    }

}
