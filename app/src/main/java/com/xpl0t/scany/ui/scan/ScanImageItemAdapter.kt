package com.xpl0t.scany.ui.scan

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.material.card.MaterialCardView
import com.xpl0t.scany.R
import com.xpl0t.scany.models.ScanImage
import io.reactivex.rxjava3.subjects.PublishSubject


class ScanImageItemAdapter(private val ctx: Context) : RecyclerView.Adapter<ScanImageItemAdapter.ViewHolder>() {

    private var lastPosition = -1

    val scanImageClicked: PublishSubject<ScanImage> = PublishSubject.create()
    private var items: List<ScanImage> = listOf()

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val card: MaterialCardView = view.findViewById(R.id.scanImageCard)
        val scanImage: ImageView = view.findViewById(R.id.scanImage)

        fun clearAnimation() {
            itemView.clearAnimation()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.scan_image_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]

        holder.scanImage.setImageBitmap(item.improved)
        Glide.with(holder.itemView)
            .load(item.improved)
            .into(holder.scanImage)

        holder.card.setOnClickListener {
            Log.d(TAG, "Scan image card clicked (id: ${item.id})")
            scanImageClicked.onNext(item)
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
            val animation: Animation =
                AnimationUtils.loadAnimation(ctx, R.anim.slide_in_top)
            view.startAnimation(animation)
            lastPosition = position
        }
    }

    fun updateItems(items: List<ScanImage>) {
        this.items = items
        notifyItemRangeChanged(0, this.items.count())
    }

    companion object {
        const val TAG = "ScanImageItemAdapter"
    }
}
