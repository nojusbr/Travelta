package com.example.travel_organiser

import android.content.Context
import android.content.Intent
import android.os.CountDownTimer
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.concurrent.TimeUnit

class PlansAdapter(
    private val context: Context,
    private var plansList: MutableList<Plan>
) : RecyclerView.Adapter<PlansAdapter.PlanViewHolder>() {

    // Define a new list for filtered plans
    private var filteredPlansList = plansList

    class PlanViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val planCard: CardView = itemView.findViewById(R.id.plan_card)
        val planTitle: TextView = itemView.findViewById(R.id.plan_title)
        val planDescription: TextView = itemView.findViewById(R.id.plan_description)
        val createdTime: TextView = itemView.findViewById(R.id.plan_created_time)
        val planTimer: TextView = itemView.findViewById(R.id.timer)
        var countDownTimer: CountDownTimer? = null
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlanViewHolder {
        val itemView = LayoutInflater.from(context).inflate(R.layout.plan_item, parent, false)
        return PlanViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: PlanViewHolder, position: Int) {
        val currentPlan = filteredPlansList[position]

        holder.planTitle.text = currentPlan.title.ifEmpty { "No title available" }
        holder.planDescription.text = currentPlan.description.ifEmpty { "No description available" }
        holder.createdTime.text =
            "${currentPlan.createdTime} ${currentPlan.createdDate}".ifEmpty { "No time available" }

        holder.countDownTimer?.cancel()
        holder.planTimer.text = "Calculating..."

        val finishTime = getFinishTimeInMillis(currentPlan.date, currentPlan.time)
        val currentTime = System.currentTimeMillis()

        if (finishTime > currentTime) {
            startTimer(
                holder,
                finishTime,
                currentPlan.isReminderChecked,
                currentPlan.planId,
                currentPlan.title  // Pass planTitle here
            )
        } else {
            holder.planTimer.text = "Finished"
        }

        // Adjust CardView width dynamically
        val layoutParams = holder.planCard.layoutParams as ViewGroup.MarginLayoutParams
        layoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT
        layoutParams.setMargins(0, 0, 0, 150) // Spacing between cards
        holder.planCard.layoutParams = layoutParams

        // Handle card click to open details
        holder.planCard.setOnClickListener {
            val intent = Intent(context, FullPlan::class.java).apply {
                putExtra("planTitle", currentPlan.title)
                putExtra("planDescription", currentPlan.description)
                putExtra("planDate", currentPlan.date)
                putExtra("planTime", currentPlan.time)
                putExtra("createdTime", currentPlan.createdTime)
                putExtra("createdDate", currentPlan.createdDate)
                putExtra("position", position)
                putExtra("reminder", currentPlan.isReminderChecked)
            }
            context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int = filteredPlansList.size

    // Update data and refresh the RecyclerView
    fun updateData(newPlans: List<Plan>) {
        filteredPlansList = newPlans.toMutableList()  // Update the filtered plans list
        notifyDataSetChanged()  // Refresh the adapter view
    }

    private fun startTimer(
        holder: PlanViewHolder,
        finishTime: Long,
        isReminderChecked: Boolean,
        planId: String,
        planTitle: String
    ) {
        val currentTime = System.currentTimeMillis()
        val timeLeft = finishTime - currentTime

        var isReminderSent = false

        holder.countDownTimer = object : CountDownTimer(timeLeft, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val days = TimeUnit.MILLISECONDS.toDays(millisUntilFinished)
                val hours = TimeUnit.MILLISECONDS.toHours(millisUntilFinished) % 24
                val minutes = TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished) % 60
                val seconds = TimeUnit.MILLISECONDS.toSeconds(millisUntilFinished) % 60

                if (!isReminderSent && isReminderChecked && hours < 1 && days <= 0) {
                    scheduleNotification(
                        context,
                        planId,
                        finishTime,
                        planTitle
                    )
                    isReminderSent = true
                }

                holder.planTimer.text = when {
                    days > 0 -> "$days days $hours hr $minutes min"
                    hours > 0 -> "$hours hr $minutes min"
                    minutes > 0 -> "$minutes min"
                    else -> "$seconds s"
                }
            }

            override fun onFinish() {
                holder.planTimer.text = "Finished"
            }
        }.start()
    }

    private fun getFinishTimeInMillis(date: String, time: String): Long {
        return try {
            val dateFormat = SimpleDateFormat(
                "yyyy/MM/dd HH:mm",
                Locale.getDefault()
            ) // Adjust format based on your stored date format
            val dateTimeString = "$date $time"
            val parsedDate = dateFormat.parse(dateTimeString)

            parsedDate?.time ?: 0L
        } catch (e: Exception) {
            e.printStackTrace()
            0L
        }
    }

    private fun scheduleNotification(
        context: Context,
        planId: String,
        finishTime: Long,
        planTitle: String
    ) {
        val sharedPreferences =
            context.getSharedPreferences("notification_prefs", Context.MODE_PRIVATE)
        val hasShownNotification = sharedPreferences.getBoolean("notification_shown_$planId", false)

        if (hasShownNotification) return

        val reminderTime = finishTime - TimeUnit.HOURS.toMillis(1)  // 1 hour before the event

        val workRequest = OneTimeWorkRequestBuilder<NotificationWorker>()
            .setInitialDelay(reminderTime - System.currentTimeMillis(), TimeUnit.MILLISECONDS)
            .setInputData(workDataOf("planId" to planId, "planTitle" to planTitle))
            .build()

        WorkManager.getInstance(context).enqueueUniqueWork(
            "notification_$planId",
            ExistingWorkPolicy.REPLACE,
            workRequest
        )

        val editor = sharedPreferences.edit()
        editor.putBoolean("notification_shown_$planId", true)
        editor.apply()
    }
}
