package com.xpl0t.scany.ui.viewsubscription

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.xpl0t.scany.R
import com.xpl0t.scany.models.BillingPlan

class BillingPlanAdapter(
    private val ctx: Context,
    private val subscribeCallback: (o: BillingPlan) -> Unit
) : RecyclerView.Adapter<BillingPlanAdapter.ViewHolder>() {

    private var items: List<BillingPlan> = listOf()

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val title: TextView = view.findViewById(R.id.title)
        val subtitle: TextView = view.findViewById(R.id.subtitle)
        val subscribe: Button = view.findViewById(R.id.subscribe)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.billing_plan_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]

        holder.subscribe.setOnClickListener {
            subscribeCallback(item)
        }

        holder.title.text = ctx.resources.getString(item.variantNameResId)
        holder.subtitle.text = ctx.resources.getString(item.costOverviewResId, item.formattedPrice)
    }

    override fun getItemCount(): Int = items.count()

    fun updateItems(items: List<BillingPlan>) {
        this.items = items
        notifyDataSetChanged()
    }

    companion object {
        const val TAG = "BillingPlanAdapter"
    }
}
