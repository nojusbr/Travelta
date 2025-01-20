package com.example.travel_organiser

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Typeface
import android.os.Build
import android.os.Bundle
import android.util.TypedValue
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class MainActivity : AppCompatActivity() {
    private val sharedPreferencesName = "TravelPlan"
    private val plansKey = "plansList"

    @RequiresApi(Build.VERSION_CODES.O)
    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        var index = 1
        val add_btn: Button = findViewById(R.id.add_btn)
        val del_btn: Button = findViewById(R.id.del_btn)
        val enterPlan: EditText = findViewById(R.id.enterPlan)
        val linearLayout: LinearLayout = findViewById(R.id.linearLayout)

        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
        val current = LocalDateTime.now().format(formatter)

        val savedPlans = loadPlans()
        savedPlans.forEachIndexed { i, plan ->
            addPlanToLayout(linearLayout, plan, i + 1)
        }
        index = savedPlans.size + 1

        add_btn.setOnClickListener {
            val enterPlanText = enterPlan.text.toString()
            if(enterPlanText.isNotBlank()) {
                addPlanToLayout(linearLayout, enterPlanText, index)
                savePlan(enterPlanText)
                index++
                enterPlan.text.clear()
            }
        }

        del_btn.setOnClickListener {
            val childCount = linearLayout.childCount
            if (childCount > 0) {
                linearLayout.removeViewAt(childCount - 1)
                removeLastPlan()
                if (index > 1) index--
            }
        }
    }

    private fun addPlanToLayout(linearLayout: LinearLayout, plan: String, index: Int) {
        val newPlanText = TextView(this)
        newPlanText.text = "$index. $plan"
        newPlanText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 30f)
        newPlanText.typeface = Typeface.create("sans-serif", Typeface.NORMAL)
        linearLayout.addView(newPlanText)
    }


    private fun savePlan(plan: String) {
        val sharedPreferences = getSharedPreferences(sharedPreferencesName, Context.MODE_PRIVATE)
        val existingPlans = loadPlans().toMutableList()
        existingPlans.add(plan)
        sharedPreferences.edit().putString(plansKey, Gson().toJson(existingPlans)).apply()
    }

    private fun removeLastPlan(){
        val sharedPreferences = getSharedPreferences(sharedPreferencesName, Context.MODE_PRIVATE)
        val existingPlans = loadPlans().toMutableList()
        if(existingPlans.isNotEmpty()){
            existingPlans.removeAt(existingPlans.size - 1)
            sharedPreferences.edit().putString(plansKey, Gson().toJson(existingPlans)).apply()
        }
    }

    private fun loadPlans(): List<String> {
        val sharedPreferences = getSharedPreferences(sharedPreferencesName, Context.MODE_PRIVATE)
        val json = sharedPreferences.getString(plansKey, "[]")
        val type = object : TypeToken<List<String>>() {}.type
        return Gson().fromJson(json, type)
    }

}
