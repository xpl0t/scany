package com.xpl0t.scany.ui.viewsubscription

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.xpl0t.scany.R

class BenefitAdapter : RecyclerView.Adapter<BenefitAdapter.ViewHolder>() {

    private var items: List<String> = listOf()

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val benefitText: TextView = view.findViewById(R.id.benefit)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.benefit_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]

        holder.benefitText.text = item
    }

    override fun getItemCount(): Int = items.count()

    fun updateItems(items: List<String>) {
        this.items = items
        notifyDataSetChanged()
    }

    companion object {
        const val TAG = "BenefitAdapter"
    }
}
