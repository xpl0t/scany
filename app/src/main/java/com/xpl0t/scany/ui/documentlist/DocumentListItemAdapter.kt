package com.xpl0t.scany.ui.documentlist

import android.content.Context
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.MotionEvent.ACTION_UP
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.OnItemTouchListener
import com.google.android.material.card.MaterialCardView
import com.xpl0t.scany.R
import com.xpl0t.scany.models.Document
import io.reactivex.rxjava3.subjects.PublishSubject

class DocumentListItemAdapter constructor(
    private val context: Context
) : RecyclerView.Adapter<DocumentListItemAdapter.ViewHolder>() {

    val documentClicked = PublishSubject.create<Document>()
    private var selectedItemId: Int? = null
    private var items: List<Document> = listOf()

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val card: MaterialCardView = view.findViewById(R.id.card)
        val title: TextView = view.findViewById(R.id.title)
        val subtitle: TextView = view.findViewById(R.id.subtitle)
        val pageList: RecyclerView = view.findViewById(R.id.page_list)
        val pageAdapter = DocumentListItemPageAdapter(view.context)

        init {
            pageList.adapter = pageAdapter
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.document_list_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        val pageCount = item.pages.count()

        holder.card.isChecked = selectedItemId == item.id
        holder.card.setOnClickListener {
            documentClicked.onNext(item)
        }

        val motionEvents = mutableListOf<Pair<Float, Float>>()

        holder.pageList.addOnItemTouchListener(object: OnItemTouchListener {
            override fun onInterceptTouchEvent(rv: RecyclerView, e: MotionEvent): Boolean {
                motionEvents.add(Pair(e.x, e.y))

                if (e.action == ACTION_UP) {
                    val xDistance = motionEvents.maxOf { it.first } - motionEvents.minOf { it.first }
                    val yDistance = motionEvents.maxOf { it.second } - motionEvents.minOf { it.second }

                    if (xDistance < 50 && yDistance < 50)
                        holder.card.performClick()

                    motionEvents.clear()
                    return false
                }

                return false
            }

            override fun onTouchEvent(rv: RecyclerView, e: MotionEvent) {}

            override fun onRequestDisallowInterceptTouchEvent(disallowIntercept: Boolean) {}
        })

        holder.pageList.setOnClickListener {
            holder.card.performClick()
        }

        holder.title.text = item.name
        holder.subtitle.text = if (pageCount == 1) context.resources.getString(R.string.single_page_count, pageCount)
            else context.resources.getString(R.string.multiple_page_count, pageCount)

        holder.pageList.visibility = if (pageCount > 0) View.VISIBLE else View.GONE
        holder.pageAdapter.updateItems(item.pages)
    }

    override fun getItemCount(): Int = items.count()

    fun updateItems(items: List<Document>) {
        this.items = items
        notifyDataSetChanged()
    }

    fun selectItem(itemId: Int?) {
        val previousItem = selectedItemId
        selectedItemId = itemId

        val previousItemIdx = items.indexOfFirst { previousItem == it.id }
        val currentItemIdx = items.indexOfFirst { itemId == it.id }

        if (previousItemIdx != -1)
            notifyItemChanged(previousItemIdx)
        if (currentItemIdx != -1)
            notifyItemChanged(currentItemIdx)
    }

    companion object {
        const val TAG = "DocumentListItemAdapter"
    }
}
