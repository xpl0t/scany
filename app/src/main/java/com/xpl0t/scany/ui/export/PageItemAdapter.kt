package com.xpl0t.scany.ui.export

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.material.card.MaterialCardView
import com.xpl0t.scany.R
import com.xpl0t.scany.models.Page
import io.reactivex.rxjava3.subjects.PublishSubject


class PageItemAdapter constructor(
    private val pageSelectionService: PageSelectionService
) : RecyclerView.Adapter<PageItemAdapter.ViewHolder>() {

    private var items: MutableList<Page> = mutableListOf()

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val pageCard: MaterialCardView = view.findViewById(R.id.pageCard)
        val pageImage: ImageView = view.findViewById(R.id.pageImage)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.export_page_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]

        Glide.with(holder.itemView)
            .load(item)
            .into(holder.pageImage)

        holder.pageCard.isChecked = pageSelectionService.isSelected(item.id)
        holder.pageCard.setOnClickListener {
            holder.pageCard.isChecked = !holder.pageCard.isChecked
            pageSelectionService.setSelected(item.id, holder.pageCard.isChecked)
        }
    }

    override fun getItemCount(): Int = items.count()

    fun updateItems(items: List<Page>) {
        if (compareLists(items, this.items)) return

        this.items = items.toMutableList()
        notifyItemRangeChanged(0, this.items.count())
    }

    fun clearSelection() {
        val deselectedPages = pageSelectionService.getDeselectedPages()

        for (i in items)
            pageSelectionService.setSelected(i.id, false)

        for (i in items.indices) {
            if (deselectedPages.contains(items[i].id))
                continue // Item was already deselected

            notifyItemChanged(i)
        }
    }

    fun selectAll() {
        val deselectedPages = pageSelectionService.getDeselectedPages()

        pageSelectionService.reset()

        for (i in items.indices) {
            if (!deselectedPages.contains(items[i].id))
                continue // Item was already selected

            notifyItemChanged(i)
        }
    }

    /**
     * Compare to list and return true if they equal and false otherwise.
     */
    private fun <T> compareLists(l1: List<T>, l2: List<T>): Boolean {
        if (l1.count() != l2.count()) return false

        for ((idx, value) in l1.withIndex()) {
            if (value != l2[idx])
                return false
        }

        return true
    }
}
