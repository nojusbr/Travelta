package com.example.travel_organiser

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.work.Worker
import androidx.work.WorkerParameters

class NotificationWorker(context: Context, workerParams: WorkerParameters) :
    Worker(context, workerParams) {

    override fun doWork(): Result {
        createNotificationChannel() // Ensure the channel is created before showing notification
        val planId = inputData.getString("planId") ?: return Result.failure()
        val planTitle = inputData.getString("planTitle") ?: "Your Plan"  // Default title if not found
        showPlanNotification(planId, planTitle)
        return Result.success()
    }

    private fun showPlanNotification(planId: String, planTitle: String) {
        val intent = Intent(applicationContext, MainMenu::class.java).apply {
            putExtra("openFullPlan", true)  // Flag to indicate the plan should be opened from SharedPreferences
        }

        val pendingIntent = PendingIntent.getActivity(
            applicationContext, planId.hashCode(), intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(applicationContext, "notify_channel_id")
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("Reminder for Plan $planTitle")
            .setContentText("You have an upcoming travel plan: $planTitle. Check it now!")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        val notificationManager =
            applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(planId.hashCode(), notification)
    }


    private fun createNotificationChannel() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            val channelId = "notify_channel_id"
            val channelName = "Travel Reminder Notifications"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(channelId, channelName, importance).apply {
                description = "Channel for travel plan reminders"
            }
            val notificationManager =
                applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
}
