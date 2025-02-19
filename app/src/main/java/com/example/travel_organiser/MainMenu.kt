package com.example.travel_organiser

import ItemSpacingDecoration
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.widget.SearchView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.app.NotificationCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.travel_organiser.databinding.ActivityMainMenuBinding
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class MainMenu : AppCompatActivity() {

    private lateinit var binding: ActivityMainMenuBinding
    private lateinit var plansRecyclerView: RecyclerView
    private lateinit var plansAdapter: PlansAdapter
    private var plansList: MutableList<Plan> = mutableListOf()

    companion object {
        const val REQUEST_CODE_NEW_PLAN = 100
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)

        binding = ActivityMainMenuBinding.inflate(layoutInflater)
        setContentView(binding.root)

        plansRecyclerView = binding.plansRecyclerView
        plansRecyclerView.layoutManager = GridLayoutManager(this, 1)

        val spacing = resources.getDimensionPixelSize(R.dimen.item_spacing)
        plansRecyclerView.addItemDecoration(ItemSpacingDecoration(spacing))

        plansAdapter = PlansAdapter(this, plansList)
        plansRecyclerView.adapter = plansAdapter

        plansRecyclerView.clipToPadding = false
        plansRecyclerView.clipChildren = false

        val backgroundColor = resources.getColor(R.color.backgroundColor)
        window.navigationBarColor = backgroundColor
        window.statusBarColor = backgroundColor
        window.decorView.systemUiVisibility = 0

        setSupportActionBar(binding.toolbarTop)
        supportActionBar?.title = ""

        if (intent.flags and Intent.FLAG_ACTIVITY_CLEAR_TASK != 0) {
            finishAffinity()
        }

        val searchView = findViewById<SearchView>(R.id.search_view)

        // Set up search functionality
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                filterPlans(newText.orEmpty())
                return true
            }
        })

        // Call functions to display plans and set up notifications
        displayPlans()
        createNotificationChannel()
        showNotification()

        binding.fab.setOnClickListener {
            val intent = Intent(this, PlanCreator::class.java)
            startActivityForResult(intent, REQUEST_CODE_NEW_PLAN)
        }
    }

    private fun displayPlans() {
        val sharedPref = getSharedPreferences("Plans", Context.MODE_PRIVATE)
        val plansListJson = sharedPref.getString("plansList", "[]") ?: "[]"
        val gson = Gson()
        val listType = object : TypeToken<List<Plan>>() {}.type
        val loadedPlansList: List<Plan> = gson.fromJson(plansListJson, listType)

        plansList.clear()
        plansList.addAll(loadedPlansList)
        plansAdapter.notifyDataSetChanged()
    }

    @Deprecated("Deprecated in Java") // @deprecated annotation for backward compatibility
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQUEST_CODE_NEW_PLAN && resultCode == RESULT_OK) {
            val updatedPlansListJson = data?.getStringExtra("updatedPlansList")

            updatedPlansListJson?.let {
                val gson = Gson()
                val listType = object : TypeToken<MutableList<Plan>>() {}.type
                val updatedPlansList: MutableList<Plan> = gson.fromJson(it, listType)

                plansList.clear()
                plansList.addAll(updatedPlansList)
                plansAdapter.notifyDataSetChanged()
            }
        }
    }

    private fun createNotificationChannel() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            val channelId = "app_opened_notification"
            val channelName = "Opened App Notification"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(channelId, channelName, importance).apply {
                description = "Everything set for your journey?"
            }
            val notificationManager =
                applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun showNotification() {
        val intent = Intent(applicationContext, MainMenu::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        val pendingIntent = PendingIntent.getActivity(
            applicationContext, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(applicationContext, "app_opened_notification")
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("Your travel reminder is here!")
            .setContentText("Everything set for your journey?")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setAutoCancel(false)
            .build()

        val notificationManager =
            applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(1, notification)
    }

    private fun filterPlans(query: String) {
        val filteredList = if (query.isEmpty()) {
            plansList
        } else {
            plansList.filter { plan ->
                plan.title.contains(query, ignoreCase = true)  // Adjust 'name' field as necessary
            }
        }
        plansAdapter.updateData(filteredList)
    }
}
