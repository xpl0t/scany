package com.xpl0t.scany.ui.documentlist

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.xpl0t.scany.R
import com.xpl0t.scany.models.Page

class DocumentListItemPageAdapter constructor(
    private val context: Context
) : RecyclerView.Adapter<DocumentListItemPageAdapter.ViewHolder>() {

    private var items: List<Page> = listOf()

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val pageImage: ImageView = view.findViewById(R.id.pageImage)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.document_list_item_page_preview, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]

        Glide.with(context)
            .load(item)
            .into(holder.pageImage)
    }

    override fun getItemCount(): Int = items.count()

    fun updateItems(items: List<Page>) {
        this.items = items
        notifyDataSetChanged()
    }

    companion object {
        const val TAG = "DocumentListItemPageAdapter"
    }
}