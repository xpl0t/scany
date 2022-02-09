package com.xpl0t.scany.ui.scanlist

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView
import com.xpl0t.scany.R
import com.xpl0t.scany.models.Scan
import io.reactivex.rxjava3.subjects.PublishSubject

class ScanItemAdapter : RecyclerView.Adapter<ScanItemAdapter.ViewHolder>() {

    val scanClicked = PublishSubject.create<Scan>()
    private var items: List<Scan> = listOf()

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val card: MaterialCardView = view.findViewById(R.id.scanCard)
        val title: TextView = view.findViewById(R.id.scanName)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.scan_list_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]

        holder.title.text = item.name
        holder.card.setOnClickListener {
            Log.d(TAG, "Scan card clicked (id: ${item.id})")
            scanClicked.onNext(item)
        }
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
