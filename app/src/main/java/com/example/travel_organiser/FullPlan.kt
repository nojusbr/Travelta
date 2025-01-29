package com.example.travel_organiser

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class FullPlan : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_full_plan)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val backgroundColor = resources.getColor(R.color.backgroundColor)
        window.navigationBarColor = backgroundColor
        window.statusBarColor = backgroundColor

        val backBtn: Button = findViewById(R.id.back_btn)

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

        title.text = planTitle
        description.text = planDescription
        date.text = planDate
        time.text = planTime
        createdDate.text = planCreatedDate
        createdTime.text = planCreatedTime


        backBtn.setOnClickListener {
            finish()
        }
    }
}