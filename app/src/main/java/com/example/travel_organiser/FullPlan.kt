package com.example.travel_organiser

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.travel_organiser.MainMenu.Companion.REQUEST_CODE_NEW_PLAN
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class FullPlan : AppCompatActivity() {

    private var isPlanDeleted = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_full_plan)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val backgroundColor = resources.getColor(R.color.backgroundColor)
        window.navigationBarColor = backgroundColor
        window.statusBarColor = backgroundColor
        window.decorView.systemUiVisibility = 0


        // Set up views
        val backBtn: Button = findViewById(R.id.back_btn)
        val delBtn: Button = findViewById(R.id.del_btn)
        val editBtn: Button = findViewById(R.id.edit_btn)

        val title: TextView = findViewById(R.id.title_tv)
        val description: TextView = findViewById(R.id.desc_tv)
        val date: TextView = findViewById(R.id.date_tv)
        val time: TextView = findViewById(R.id.time_tv)
        val createdTime: TextView = findViewById(R.id.created_time_tv)
        val createdDate: TextView = findViewById(R.id.created_date_tv)

        val planTitle = intent.getStringExtra("planTitle")
        val planDescription = intent.getStringExtra("planDescription")
        val planDate = intent.getStringExtra("planDate")
        val planTime = intent.getStringExtra("planTime")
        val planCreatedTime = intent.getStringExtra("createdTime") ?: "No Time"
        val planCreatedDate = intent.getStringExtra("createdDate") ?: "No Date"
        val position = intent.getIntExtra("position", -1)

        title.text = planTitle
        description.text = planDescription
        date.text = planDate
        time.text = planTime
        createdDate.text = planCreatedDate
        createdTime.text = planCreatedTime

        backBtn.setOnClickListener {
            val intent = Intent(this, MainMenu::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            startActivity(intent)
            finish()
        }

        delBtn.setOnClickListener {
            val builder: AlertDialog.Builder = AlertDialog.Builder(this)
            builder
                .setTitle("Delete the plan?")
                .setMessage("Do you really want to delete this plan?")
                .setPositiveButton("Yes") { dialog, which ->
                    if (position != -1) {
                        deletePlan(position)
                        isPlanDeleted = true // Mark as deleted
                        val intent = Intent(this, MainMenu::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP // Clear the activity stack
                        startActivity(intent)
                        finish()
                    }
                    Toast.makeText(this, "Plan deleted", Toast.LENGTH_SHORT).show()
                }
                .setNegativeButton("No, save my plan!") { dialog, which ->
                    Toast.makeText(this, "Cancelled", Toast.LENGTH_SHORT).show()
                }
            val dialog: AlertDialog = builder.create()
            dialog.show()
        }

        editBtn.setOnClickListener {
            val intent = Intent(this, PlanCreator::class.java)
            intent.putExtra("title", planTitle)
            intent.putExtra("description", planDescription)
            intent.putExtra("time", planTime)
            intent.putExtra("date", planDate)
            intent.putExtra("position", position)
            startActivity(intent)
            finish()
        }
    }


    private fun deletePlan(position: Int) {
        val sharedPref = getSharedPreferences("Plans", Context.MODE_PRIVATE)
        val editor = sharedPref.edit()

        val plansJson = sharedPref.getString("plansList", "[]")
        val gson = Gson()
        val listType = object : TypeToken<MutableList<Plan>>() {}.type
        val plansList: MutableList<Plan> = gson.fromJson(plansJson, listType)

        if (position >= 0 && position < plansList.size) {
            // Remove the plan from the list
            plansList.removeAt(position)

            // Save the updated plans list back to SharedPreferences
            val updatedPlansJson = gson.toJson(plansList)
            editor.putString("plansList", updatedPlansJson)
            editor.apply()

            // Send the updated list back to MainMenu to update the adapter
            val intent = Intent(this, MainMenu::class.java).apply {
                putExtra("updatedPlansList", updatedPlansJson)
            }
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP // Ensure the back stack is cleared
            startActivity(intent)
            finish()
        }
    }
}

