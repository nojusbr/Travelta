package com.example.travel_organiser

import ItemSpacingDecoration
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.widget.SearchView
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

        if (plansList.isEmpty()) binding.noPlansTextView.visibility = View.VISIBLE
        else binding.noPlansTextView.visibility = View.GONE
    }

    @Deprecated("Deprecated in Java")
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
