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
import io.reactivex.rxjava3.subjects.PublishSubject
import org.opencv.core.Mat

class FilterItemAdapter(
    private val ctx: Context,
    private val originalImg: Mat,
    private val filters: FilterList
) :
    RecyclerView.Adapter<FilterItemAdapter.ViewHolder>() {

    var curFilter: String? = null
    val filterSelected: PublishSubject<Filter?> = PublishSubject.create()

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val card: MaterialCardView = view.findViewById(R.id.pageCard)
        val scanImage: ImageView = view.findViewById(R.id.pageImage)

        fun clearAnimation() {
            itemView.clearAnimation()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.filter_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = filters[position]

        val scaled = originalImg.clone()
        scaled.scale(holder.itemView.height.toDouble())

        val filtered = item.apply(scaled)

        Glide.with(ctx)
            .load(filtered)
            .into(holder.scanImage)

        holder.card.setOnClickListener {
            Log.d(TAG, "Filter selected (id: ${item.id})")
            holder.card.isChecked = !holder.card.isChecked
            curFilter = if (holder.card.isChecked) item.id else null
            filterSelected.onNext(if (holder.card.isChecked) item else null)
        }
    }

    override fun getItemCount(): Int = filters.count()

    override fun onViewDetachedFromWindow(holder: ViewHolder) {
        holder.clearAnimation()
    }

    companion object {
        const val TAG = "FilterItemAdapter"
    }
}
