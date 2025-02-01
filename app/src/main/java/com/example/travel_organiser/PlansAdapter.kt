package com.example.travel_organiser

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.CountDownTimer
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.app.NotificationCompat
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

class PlansAdapter(
    private val context: Context,
    private var plansList: MutableList<Plan>
) : RecyclerView.Adapter<PlansAdapter.PlanViewHolder>() {

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
        val currentPlan = plansList[position]
        createNotificationChannel(context)

        holder.planTitle.text = currentPlan.title.ifEmpty { "No title available" }
        holder.planDescription.text = currentPlan.description.ifEmpty { "No description available" }
        holder.createdTime.text =
            "${currentPlan.createdTime} ${currentPlan.createdDate}".ifEmpty { "No time available" }

        holder.countDownTimer?.cancel()
        holder.planTimer.text = "Calculating..."

        val finishTime = getFinishTimeInMillis(currentPlan.date, currentPlan.time)
        val currentTime = System.currentTimeMillis()

        if (finishTime > currentTime) {
            startTimer(holder, finishTime, currentPlan.isReminderChecked)
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

    override fun getItemCount(): Int = plansList.size

    private fun startTimer(holder: PlanViewHolder, finishTime: Long, isReminderChecked: Boolean) {
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
                    showReminder(context)
                    isReminderSent = true
                }

                holder.planTimer.text = when {
                    days > 0 -> "${days}d ${hours}h ${minutes}m"
                    hours > 0 -> "${hours}h ${minutes}m ${seconds}s"
                    minutes > 0 -> "${minutes}m ${seconds}s"
                    else -> "${seconds}s"
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

    fun showReminder(context: Context) {
        val channelId = "my_channel_id"
        val notificationId = 1

        // Intent to open MainActivity when clicked
        val intent = Intent(context, MainMenu::class.java)
        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Build the notification
        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("1 hour left")
            .setContentText("1 hour left until the on the plan")
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        // Show the notification
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(notificationId, notification)
    }

    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channelId = "my_channel_id"
            val channelName = "My Channel"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(channelId, channelName, importance)

            val notificationManager: NotificationManager =
                context.getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }
}

