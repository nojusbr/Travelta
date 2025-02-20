package com.example.travel_organiser

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.UUID

class PlanCreator : AppCompatActivity() {

    private lateinit var planDateTv: TextView
    private lateinit var planTimeTv: TextView
    private var selectedDate: Calendar = Calendar.getInstance()
    private var position: Int = -1  // Default to -1, means no plan is being edited

    @RequiresApi(Build.VERSION_CODES.O)
    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_plan_creator)

        // Apply window insets
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Matching the status bar and navigation bar colors with the background color
        val backgroundColor = resources.getColor(R.color.backgroundColor)
        window.navigationBarColor = backgroundColor
        window.statusBarColor = backgroundColor

        // Views
        val backBtn: Button = findViewById(R.id.back_btn)
        val timeBtn: Button = findViewById(R.id.plan_time_btn)
        val dateBtn: Button = findViewById(R.id.plan_date_btn)
        val saveBtn: Button = findViewById(R.id.save_plan_btn)

        planTimeTv = findViewById(R.id.plan_time_tv)
        planDateTv = findViewById(R.id.plan_date_tv)
        val planLetterLimTv: TextView = findViewById(R.id.plan_letterLim_tv)

        val planTitleEditable: EditText = findViewById(R.id.plan_title_edt)
        val planDescEditable: EditText = findViewById(R.id.plan_desc_edt)
        val reminderCheckbox: CheckBox = findViewById(R.id.reminder_chkbx)

        val maxChars = 300

        // Retrieve the position if editing an existing plan
        position = intent.getIntExtra("position", -1)

        // If editing, populate fields with existing data
        if (position != -1) {
            val existingPlan = getPlanFromPosition(position)
            planTitleEditable.setText(existingPlan?.title)
            planDescEditable.setText(existingPlan?.description)
            planDateTv.text = existingPlan?.date
            planTimeTv.text = existingPlan?.time
        }

        // Date Picker
        dateBtn.setOnClickListener {
            val calendar = Calendar.getInstance()
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH)
            val day = calendar.get(Calendar.DAY_OF_MONTH)

            val datePickerDialog = DatePickerDialog(
                this,
                { _, selectedYear, selectedMonth, selectedDay ->
                    selectedDate.set(selectedYear, selectedMonth, selectedDay)
                    val formattedDate = "$selectedYear/${selectedMonth + 1}/$selectedDay"
                    planDateTv.text = formattedDate
                },
                year,
                month,
                day
            )
            datePickerDialog.datePicker.minDate = System.currentTimeMillis() // Prevents past dates
            datePickerDialog.show()
        }

        // Time Picker
        timeBtn.setOnClickListener {
            val currentTime = Calendar.getInstance()
            val currentHour = currentTime.get(Calendar.HOUR_OF_DAY)
            val currentMinute = currentTime.get(Calendar.MINUTE)

            val isToday = isSameDay(selectedDate, currentTime)

            val timePickerDialog = TimePickerDialog(
                this,
                { _, hourOfDay, minute ->
                    if (isToday && (hourOfDay < currentHour || (hourOfDay == currentHour && minute < currentMinute))) {
                        Toast.makeText(this, "Cannot select a past time", Toast.LENGTH_SHORT).show()
                    } else {
                        planTimeTv.text = String.format("%02d:%02d", hourOfDay, minute)
                    }
                },
                currentHour,
                currentMinute,
                true
            )

            timePickerDialog.show()
        }

        // Character Limit for Description
        planDescEditable.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val remainingChars = maxChars - (s?.length ?: 0)
                planLetterLimTv.text = "$remainingChars/300"
                planLetterLimTv.setTextColor(if (remainingChars <= 0) Color.RED else Color.DKGRAY)
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        backBtn.setOnClickListener { finish() }

        saveBtn.setOnClickListener {
            val title = planTitleEditable.text.toString()
            val description = planDescEditable.text.toString()
            val date = planDateTv.text.toString()
            val time = planTimeTv.text.toString()
            val isReminderChecked = reminderCheckbox.isChecked
            val createdDate = getCurrentDate()
            val createdTime = getCurrentTime()

            if (title.isNotBlank() && description.isNotBlank() && date.isNotBlank() && time.isNotBlank()) {
                val planId = UUID.randomUUID().toString()
                val plan = Plan(
                    planId,
                    title,
                    description,
                    date,
                    time,
                    createdDate,
                    createdTime,
                    isReminderChecked
                )
                savePlan(plan)
            } else {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun getPlanFromPosition(position: Int): Plan? {
        val sharedPref = getSharedPreferences("Plans", Context.MODE_PRIVATE)
        val gson = Gson()
        val plansJson = sharedPref.getString("plansList", "[]")
        val listType = object : TypeToken<MutableList<Plan>>() {}.type
        val plansList: MutableList<Plan> = gson.fromJson(plansJson, listType)
        return if (position >= 0 && position < plansList.size) {
            plansList[position]
        } else null
    }

    private fun isSameDay(cal1: Calendar, cal2: Calendar): Boolean {
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)
    }

    private fun savePlan(plan: Plan) {
        val sharedPref = getSharedPreferences("Plans", Context.MODE_PRIVATE)
        val gson = Gson()
        val plansJson = sharedPref.getString("plansList", "[]")
        val listType = object : TypeToken<MutableList<Plan>>() {}.type
        val plansList: MutableList<Plan> = gson.fromJson(plansJson, listType)

        // If editing, replace the existing plan at position
        if (position != -1) {
            plansList[position] = plan
        } else {
            plansList.add(plan)
        }

        val updatedPlansJson = gson.toJson(plansList)
        sharedPref.edit().putString("plansList", updatedPlansJson).apply()

        val resultIntent = Intent()
        resultIntent.putExtra("updatedPlansList", updatedPlansJson)
        setResult(RESULT_OK, resultIntent)


        val intent = Intent(this, MainMenu::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
        startActivity(intent)
        finish()
    }

    private fun getCurrentTime(): String {
        val sdf = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
        return sdf.format(Date())
    }

    private fun getCurrentDate(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return sdf.format(Date())
    }
}
