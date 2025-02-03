package com.example.travel_organiser

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        if (intent?.action == Intent.ACTION_BOOT_COMPLETED) {

            val workRequest = PeriodicWorkRequestBuilder<NotificationWorker>(12, TimeUnit.HOURS)
                .setInitialDelay(1, TimeUnit.MINUTES) // For testing, you might reduce this value
                .build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                "travel_notification",
                ExistingPeriodicWorkPolicy.REPLACE,
                workRequest
            )
        }
    }
}
