package com.xpl0t.scany.ui.reorderpages

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


class PageItemAdapter : RecyclerView.Adapter<PageItemAdapter.ViewHolder>(),
    PageMoveCallback.Contract {

    val pageOrderChanged: PublishSubject<List<Page>> = PublishSubject.create()
    private var items: MutableList<Page> = mutableListOf()

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val pageCard: MaterialCardView = view.findViewById(R.id.pageCard)
        val pageImage: ImageView = view.findViewById(R.id.pageImage)

        fun clearAnimation() {
            itemView.clearAnimation()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.reorder_pages_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]

        Glide.with(holder.itemView)
            .load(item)
            .into(holder.pageImage)
    }

    override fun getItemCount(): Int = items.count()

    override fun onViewDetachedFromWindow(holder: ViewHolder) {
        holder.clearAnimation()
    }

    fun updateItems(items: List<Page>) {
        if (compareLists(items, this.items)) return

        this.items = items.toMutableList()
        notifyItemRangeChanged(0, this.items.count())
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

    override fun onRowMoved(fromPosition: Int, toPosition: Int) {
        val item = items.removeAt(fromPosition)
        items.add(toPosition, item)

        pageOrderChanged.onNext(items)
        notifyItemMoved(fromPosition, toPosition)
    }

    override fun onRowSelected(viewHolder: ViewHolder) {
        viewHolder.pageCard.isDragged = true
    }

    override fun onRowClear(viewHolder: ViewHolder) {
        viewHolder.pageCard.isDragged = false
    }
}
