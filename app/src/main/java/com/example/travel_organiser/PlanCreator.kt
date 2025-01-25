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
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.TextView
import android.widget.TimePicker
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class PlanCreator : AppCompatActivity() {

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

        val planTimeTv: TextView = findViewById(R.id.plan_time_tv)
        val planDateTv: TextView = findViewById(R.id.plan_date_tv)
        val planLetterLimTv: TextView = findViewById(R.id.plan_letterLim_tv)

        val planTitleEditable: EditText = findViewById(R.id.plan_title_edt)
        val planDescEditable: EditText = findViewById(R.id.plan_desc_edt)

        val reminderCheckbox: CheckBox = findViewById(R.id.reminder_chkbx)

        val maxChars = 120

        // Date Picker Listener
        val datePickerDialogListener =
            DatePickerDialog.OnDateSetListener { _, year, monthOfYear, dayOfMonth ->
                val formattedDate = "${dayOfMonth}/${monthOfYear + 1}/$year"
                planDateTv.text = formattedDate
            }

        // Time Picker Listener
        val timePickerDialogListener: TimePickerDialog.OnTimeSetListener =
            object : TimePickerDialog.OnTimeSetListener {
                override fun onTimeSet(view: TimePicker?, hourOfDay: Int, minute: Int) {
                    val formattedTime: String = when {
                        minute < 10 -> {
                            "${hourOfDay}:0${minute}"
                        }

                        else -> {
                            "${hourOfDay}:${minute}"
                        }
                    }
                    planTimeTv.text = formattedTime
                }
            }

        planDescEditable.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val remainingChars = maxChars - (s?.length ?: 0)
                planLetterLimTv.text = "$remainingChars/120"
                if (remainingChars <= 0) planLetterLimTv.setTextColor(Color.RED)
                else planLetterLimTv.setTextColor(Color.DKGRAY)
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        // Set up Time Button click listener
        timeBtn.setOnClickListener {
            val timePicker: TimePickerDialog =
                TimePickerDialog(this, timePickerDialogListener, 12, 10, true)
            timePicker.show()
        }

        // Set up Date Button click listener
        dateBtn.setOnClickListener {
            val calendar = java.util.Calendar.getInstance()
            val year = calendar.get(java.util.Calendar.YEAR)
            val month = calendar.get(java.util.Calendar.MONTH)
            val day = calendar.get(java.util.Calendar.DAY_OF_MONTH)

            val datePickerDialog = DatePickerDialog(
                this,
                datePickerDialogListener,
                year,
                month,
                day
            )
            datePickerDialog.show()
        }

        // Set up Back Button click listener
        backBtn.setOnClickListener {
            val intent = Intent(this, MainMenu::class.java)
            startActivity(intent)
        }

        saveBtn.setOnClickListener {
            val title = planTitleEditable.text.toString()
            val description = planDescEditable.text.toString()
            val date = planDateTv.text.toString()
            val time = planTimeTv.text.toString()
            val isReminderChecked = reminderCheckbox.isChecked

            if (title.isNotBlank() && description.isNotBlank() && date.isNotBlank() && time.isNotBlank()) {
                val plan = Plan(title, description, date, time, isReminderChecked)
                savePlan(plan)
            } else {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
            }

        }

    }

    private fun savePlan(plan: Plan) {
        val sharedPref = getSharedPreferences("Plans", Context.MODE_PRIVATE)
        val editor = sharedPref.edit()

        // Get the existing plans list
        val plansListJson = sharedPref.getString("plansList", "[]") ?: "[]"
        val gson = Gson()
        val listType = object : TypeToken<MutableList<Plan>>() {}.type
        val plansList: MutableList<Plan> = gson.fromJson(plansListJson, listType)

        // Add the new plan to the list
        plansList.add(plan)

        // Save the updated list back to SharedPreferences
        val updatedPlansListJson = gson.toJson(plansList)
        editor.putString("plansList", updatedPlansListJson)
        editor.apply()

        // Return the updated plans list to MainMenu
        val resultIntent = Intent()
        resultIntent.putExtra("updatedPlansList", updatedPlansListJson)
        setResult(RESULT_OK, resultIntent)
        finish() // Finish the activity and return to MainMenu
    }



    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_DOWN) {
            val focusedView = currentFocus
            if (focusedView != null && focusedView is EditText) {
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

}
