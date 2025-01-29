package com.example.travel_organiser

import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView

class PlansAdapter(
    private val context: Context,
    private var plansList: MutableList<Plan>
) : RecyclerView.Adapter<PlansAdapter.PlanViewHolder>() {

    inner class PlanViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val planCard: CardView = itemView.findViewById(R.id.plan_card)
        val planTitle: TextView = itemView.findViewById(R.id.plan_title)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlanViewHolder {
        val itemView = LayoutInflater.from(context).inflate(R.layout.plan_item, parent, false)
        return PlanViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: PlanViewHolder, position: Int) {
        val currentPlan = plansList[position]
        val title = currentPlan.title.ifEmpty { "No title available" }

        holder.planTitle.text = title

        // Adjust card size based on text width
        try {
            val textPaint = holder.planTitle.paint
            val textWidth = textPaint.measureText(title).toInt()
            val padding = context.resources.getDimensionPixelSize(R.dimen.card_padding)

            val cardWidth = textWidth + padding * 2
            val minCardWidth = context.resources.getDimensionPixelSize(R.dimen.min_card_width)
            val finalCardWidth = maxOf(cardWidth, minCardWidth)
            val layoutParams = holder.planCard.layoutParams as ViewGroup.MarginLayoutParams
            layoutParams.width = finalCardWidth
            layoutParams.setMargins(-20, 0, 0, 8) // Add margins around cards
            holder.planCard.layoutParams = layoutParams

            holder.planCard.setOnClickListener {
                val intent = Intent(context, FullPlan::class.java)
                intent.putExtra("planTitle", currentPlan.title)
                intent.putExtra("planDescription", currentPlan.description)
                intent.putExtra("planDate", currentPlan.date)
                intent.putExtra("planTime", currentPlan.time)
                intent.putExtra("createdTime", currentPlan.createdTime)
                intent.putExtra("createdDate", currentPlan.createdDate)

                context.startActivity(intent)
            }

        } catch (e: Exception) {
            Log.e("PlansAdapter", "Error in onBindViewHolder", e)
        }
    }

    override fun getItemCount(): Int = plansList.size
}
