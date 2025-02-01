package com.example.travel_organiser

import ItemSpacingDecoration
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.isInvisible
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

        //call functions
        displayPlans()


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

    @Deprecated("Deprecated in Java") // ?? no idea what this does but wtv
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQUEST_CODE_NEW_PLAN && resultCode == RESULT_OK) {
            // Retrieve the updated plans list from the intent
            val updatedPlansListJson = data?.getStringExtra("updatedPlansList")

            updatedPlansListJson?.let {
                // Convert the JSON string to a MutableList<Plan> using Gson
                val gson = Gson()
                val listType = object : TypeToken<MutableList<Plan>>() {}.type
                val updatedPlansList: MutableList<Plan> = gson.fromJson(it, listType)

                // Update the current plans list and refresh the adapter
                plansList.clear()
                plansList.addAll(updatedPlansList)
                plansAdapter.notifyDataSetChanged()  // Notify the adapter that the data has changed
            }
        }
    }
}
