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
import android.view.MotionEvent
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.text.SimpleDateFormat
import java.util.*

class PlanCreator : AppCompatActivity() {

    private lateinit var planDateTv: TextView
    private lateinit var planTimeTv: TextView
    private var selectedDate: Calendar = Calendar.getInstance()

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
                val plan = Plan(title, description, date, time, isReminderChecked, createdDate, createdTime)
                savePlan(plan)
            } else {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun isSameDay(cal1: Calendar, cal2: Calendar): Boolean {
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)
    }

    private fun savePlan(plan: Plan) {
        val sharedPref = getSharedPreferences("Plans", Context.MODE_PRIVATE)
        val editor = sharedPref.edit()

        val plansJson = sharedPref.getString("plansList", "[]")
        val gson = Gson()
        val listType = object : TypeToken<MutableList<Plan>>() {}.type
        val plansList: MutableList<Plan> = gson.fromJson(plansJson, listType)

        plansList.add(plan)

        val updatedPlansJson = gson.toJson(plansList)
        editor.putString("plansList", updatedPlansJson)
        editor.apply()

        val resultIntent = Intent()
        resultIntent.putExtra("updatedPlansList", updatedPlansJson)
        setResult(RESULT_OK, resultIntent)

        startActivity(Intent(this, MainMenu::class.java))
        finish()
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_DOWN) {
            val focusedView = currentFocus
            if (focusedView is EditText) {
                hideKeyboard(focusedView)
                focusedView.clearFocus()
            }
        }
        return super.onTouchEvent(event)
    }

    private fun hideKeyboard(editText: EditText) {
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(editText.windowToken, 0)
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
