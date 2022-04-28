package com.xpl0t.scany.ui.addpage.improve

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.material.card.MaterialCardView
import com.xpl0t.scany.R
import com.xpl0t.scany.extensions.scale
import com.xpl0t.scany.filter.Filter
import com.xpl0t.scany.filter.FilterList
import com.xpl0t.scany.util.Optional
import io.reactivex.rxjava3.subjects.PublishSubject
import org.opencv.core.Mat

class FilterItemAdapter(
    private val ctx: Context,
    private val originalImg: Mat,
    private val filters: FilterList
) :
    RecyclerView.Adapter<FilterItemAdapter.ViewHolder>() {

    val viewHolders = mutableListOf<ViewHolder>()

    var curFilter: String? = null
    val filterSelected: PublishSubject<Optional<Filter>> = PublishSubject.create()

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val card: MaterialCardView = view.findViewById(R.id.pageCard)
        val scanImage: ImageView = view.findViewById(R.id.pageImage)
        var filter: Filter? = null
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.filter_item, parent, false)
        val viewHolder = ViewHolder(view)
        viewHolders.add(viewHolder)

        return viewHolder
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = filters[position]
        holder.filter = item

        val scaled = originalImg.clone()
        scaled.scale(150.0)

        val filtered = item.apply(scaled)

        Glide.with(ctx)
            .load(filtered)
            .into(holder.scanImage)

        holder.card.isChecked = curFilter == item.id

        holder.card.setOnClickListener {
            Log.d(TAG, "Filter selected (id: ${item.id})")

            val newState = !holder.card.isChecked
            setCurrentFilter(if(newState) item else null)
            filterSelected.onNext(if (newState) Optional(item) else Optional.empty())
        }
    }

    fun setCurrentFilter(filter: Filter?) {
        curFilter = filter?.id

        for (vh in viewHolders) {
            vh.card.isChecked = vh.filter?.id == curFilter
        }
    }

    override fun getItemCount(): Int = filters.count()

    override fun onViewDetachedFromWindow(holder: ViewHolder) {
        viewHolders.remove(holder)
    }

    companion object {
        const val TAG = "FilterItemAdapter"
    }
}
