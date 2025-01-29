package com.example.travel_organiser

import ItemSpacingDecoration
import android.content.Context
import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
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

        displayPlans()

        val backgroundColor = resources.getColor(R.color.backgroundColor)
        window.navigationBarColor = backgroundColor
        window.statusBarColor = backgroundColor

        setSupportActionBar(binding.toolbarTop)

        val customFont: Typeface? = ResourcesCompat.getFont(this, R.font.nunito_bolditalic)

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
            val updatedPlansListJson = data?.getStringExtra("updatedPlansList")
            val gson = Gson()
            val listType = object : TypeToken<MutableList<Plan>>() {}.type
            val updatedPlansList: MutableList<Plan> = gson.fromJson(updatedPlansListJson, listType)
            plansList.clear()
            plansList.addAll(updatedPlansList)
            plansAdapter.notifyDataSetChanged()
        }
    }

    fun savePlanToPrefs(newPlan: Plan) {
        val sharedPref = getSharedPreferences("Plans", Context.MODE_PRIVATE)
        val editor = sharedPref.edit()
        val position = plansList.size
        plansList.add(newPlan)
        val gson = Gson()
        val json = gson.toJson(plansList)
        editor.putString("plansList", json)
        editor.apply()
        plansAdapter.notifyItemInserted(position)

    }
}
