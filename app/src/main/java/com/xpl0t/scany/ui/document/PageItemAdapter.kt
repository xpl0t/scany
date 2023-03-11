package com.xpl0t.scany.ui.document

import android.content.Context
import android.util.Log
import android.view.ContextMenu
import android.view.Gravity
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.PopupMenu
import androidx.annotation.MenuRes
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.material.card.MaterialCardView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.xpl0t.scany.R
import com.xpl0t.scany.models.Page
import io.reactivex.rxjava3.subjects.PublishSubject


class PageItemAdapter(private val ctx: Context, private val deletePageCallback: ((pageId: Int) -> Unit)) :
    RecyclerView.Adapter<PageItemAdapter.ViewHolder>() {

    private var lastPosition = -1

    val pageClicked: PublishSubject<Page> = PublishSubject.create()
    private var items: List<Page> = listOf()

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val card: MaterialCardView = view.findViewById(R.id.pageCard)
        val documentImage: ImageView = view.findViewById(R.id.pageImage)

        fun clearAnimation() {
            itemView.clearAnimation()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.page_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]

        Glide.with(ctx)
            .load(item)
            .into(holder.documentImage)

        holder.card.setOnClickListener {
            Log.d(TAG, "Document image card clicked (id: ${item.id})")
            pageClicked.onNext(item)
        }
        holder.card.setOnLongClickListener {
            Log.d(TAG, "Document image card long clicked (id: ${item.id})")
            showPageContextMenu(item.id)
            true
        }
        setAnimation(holder.itemView, position)
    }

    override fun getItemCount(): Int = items.count()

    override fun onViewDetachedFromWindow(holder: ViewHolder) {
        holder.clearAnimation()
    }

    // Source: https://stackoverflow.com/a/26748274
    private fun setAnimation(view: View, position: Int) {
        // If the bound view wasn't previously displayed on screen, it's animated
        if (position > lastPosition) {
            val animation = AnimationUtils.loadAnimation(ctx, android.R.anim.slide_in_left)
            view.startAnimation(animation)
            lastPosition = position
        }
    }

    fun updateItems(items: List<Page>) {
        if (compareLists(items, this.items)) return

        this.items = items
        notifyDataSetChanged()
    }

    fun resetAnimation() {
        lastPosition = -1
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

    private fun showPageContextMenu(pageId: Int) {
        val actions = mapOf<String, (() -> Unit)>(
            ctx.resources.getString(R.string.delete_btn) to { deletePageCallback(pageId) }
        )

        MaterialAlertDialogBuilder(ctx)
            .setTitle(R.string.page_card_context_menu_title)
            .setItems(actions.keys.toTypedArray()) { _, item ->
                val action = actions.values.toList()[item]
                action.invoke()
            }
            .show()
    }

    companion object {
        const val TAG = "PageItemAdapter"
    }
}
