package com.example.travel_organiser

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class PlansAdapter(
    private val context: Context,
    private var plansList: MutableList<Plan> // Make the list mutable so it can be updated
) : RecyclerView.Adapter<PlansAdapter.PlanViewHolder>() {

    // Create the ViewHolder that will hold references to the views of each item
    inner class PlanViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val planTextView: TextView = itemView.findViewById(R.id.planItemText)
    }

    // Create new views (invoked by the layout manager)
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlanViewHolder {
        val itemView = LayoutInflater.from(context).inflate(R.layout.plan_item, parent, false)
        return PlanViewHolder(itemView)
    }

    // Replace the contents of a view (invoked by the layout manager)
    override fun onBindViewHolder(holder: PlanViewHolder, position: Int) {
        val currentPlan = plansList[position]
        holder.planTextView.text = "${currentPlan.title}\n${currentPlan.date} - ${currentPlan.time}\n${currentPlan.description}"
    }

    // Return the size of the data set
    override fun getItemCount(): Int {
        return plansList.size
    }

    // Method to update the plans list and notify the adapter
    fun updatePlans(newPlansList: List<Plan>) {
        plansList.clear() // Clear the existing list
        plansList.addAll(newPlansList) // Add the new list of plans
        notifyDataSetChanged() // Notify the adapter that the data set has changed
    }
}
