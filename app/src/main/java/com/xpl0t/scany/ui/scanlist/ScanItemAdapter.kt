package com.xpl0t.scany.ui.scanlist

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.xpl0t.scany.R
import com.xpl0t.scany.models.Scan
import io.reactivex.rxjava3.subjects.PublishSubject

class ScanItemAdapter : RecyclerView.Adapter<ScanItemAdapter.ViewHolder>() {

    val scanClicked = PublishSubject.create<Scan>()
    private var items: List<Scan> = listOf()

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val selectScanBtn: MaterialButton = view.findViewById(R.id.selectScan)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.scan_list_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]

        holder.selectScanBtn.text = item.name
    }

    override fun getItemCount(): Int = items.count()

    fun updateItems(items: List<Scan>) {
        this.items = items
        notifyDataSetChanged()
    }

    companion object {
        const val TAG = "ScanItemAdapter"
    }
}
