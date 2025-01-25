package com.example.travel_organiser

import android.content.Context
import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.LinearLayoutManager
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

        // Initialize the binding and set the layout
        binding = ActivityMainMenuBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize plansRecyclerView after setContentView
        plansRecyclerView = binding.plansRecyclerView

        // Set up RecyclerView
        plansRecyclerView.layoutManager = LinearLayoutManager(this)

        // Initialize the adapter
        plansAdapter = PlansAdapter(this, plansList)
        plansRecyclerView.adapter = plansAdapter

        // Load plans from SharedPreferences
        displayPlans()

        // Set up toolbar and status bar colors
        val backgroundColor = resources.getColor(R.color.backgroundColor)
        window.navigationBarColor = backgroundColor
        window.statusBarColor = backgroundColor

        // Set up the toolbar as the support ActionBar
        setSupportActionBar(binding.toolbarTop)
        supportActionBar?.title = "Welcome back!"

        // Load custom font
        val customFont: Typeface? = ResourcesCompat.getFont(this, R.font.nunito_bolditalic)
        for (i in 0 until binding.toolbarTop.childCount) {
            val view = binding.toolbarTop.getChildAt(i)
            if (view is TextView && view.text == "Welcome back!") {
                view.typeface = customFont
                view.textSize = 32f
                break
            }
        }

        // Floating Action Button click listener to create a new plan
        binding.fab.setOnClickListener {
            val intent = Intent(this, PlanCreator::class.java)
            startActivityForResult(intent, REQUEST_CODE_NEW_PLAN)  // Start PlanCreator activity
        }
    }

    private fun displayPlans() {
        // Get the saved plans from SharedPreferences
        val sharedPref = getSharedPreferences("Plans", Context.MODE_PRIVATE)
        val plansListJson = sharedPref.getString("plansList", "[]") ?: "[]"
        val gson = Gson()

        // Correctly specify the type using TypeToken
        val listType = object : TypeToken<List<Plan>>() {}.type
        val loadedPlansList: List<Plan> = gson.fromJson(plansListJson, listType)

        // Update the plansList
        plansList.clear() // Clear any existing data
        plansList.addAll(loadedPlansList) // Add the new plans

        // Notify the adapter that data has changed
        plansAdapter.notifyDataSetChanged()
    }

    // Handle the result from PlanCreator activity
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQUEST_CODE_NEW_PLAN && resultCode == RESULT_OK) {
            // Get the updated plans list from the result
            val updatedPlansListJson = data?.getStringExtra("updatedPlansList")
            val gson = Gson()
            val listType = object : TypeToken<MutableList<Plan>>() {}.type
            val updatedPlansList: MutableList<Plan> = gson.fromJson(updatedPlansListJson, listType)

            // Update the plans list and notify the adapter
            plansList.clear() // Clear the existing list
            plansList.addAll(updatedPlansList) // Add the new list of plans
            plansAdapter.notifyDataSetChanged() // Notify the adapter to update the view
        }
    }

    // Method to save a new plan to SharedPreferences (you might call this after adding a plan)
    fun savePlanToPrefs(newPlan: Plan) {
        val sharedPref = getSharedPreferences("Plans", Context.MODE_PRIVATE)
        val editor = sharedPref.edit()

        // Add new plan to the list
        val position = plansList.size // Get the position where the new plan will be inserted
        plansList.add(newPlan)

        // Convert the plans list to JSON and save it
        val gson = Gson()
        val json = gson.toJson(plansList)
        editor.putString("plansList", json)
        editor.apply()

        // Notify the adapter that a new item was inserted
        plansAdapter.notifyItemInserted(position) // Notify the adapter that a new plan was added
    }
}
