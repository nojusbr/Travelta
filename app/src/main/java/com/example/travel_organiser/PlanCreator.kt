package com.example.travel_organiser

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.TimePicker
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class PlanCreator : AppCompatActivity() {
    private val sharedPreferencesName = "TravelPlan"
    private val plansKey = "plansList"

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

        // Views
        val backBtn: Button = findViewById(R.id.back_btn)
        val timeBtn: Button = findViewById(R.id.plan_time_btn)
        val dateBtn: Button = findViewById(R.id.plan_date_btn)

        var planTimeTv: TextView = findViewById(R.id.plan_time_tv)
        var planDateTv: TextView = findViewById(R.id.plan_date_tv)

        // Date Picker Listener
        val datePickerDialogListener =
            DatePickerDialog.OnDateSetListener { view, year, monthOfYear, dayOfMonth ->
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

        // Commented code for later use:
        /*
        //var index: Int
        //val addBtn: Button = findViewById(R.id.add_btn)
        //val delBtn: Button = findViewById(R.id.del_btn)
        //val enterPlan: EditText = findViewById(R.id.enterPlan)
        val backBtn: Button = findViewById(R.id.back_btn)
        val timeBtn: Button = findViewById(R.id.plan_time_btn)
        val dateBtn: Button = findViewById(R.id.plan_date_btn)
        //val linearLayout: LinearLayout = findViewById(R.id.linearLayout)

        var planTimeTv: TextView = findViewById(R.id.plan_time_tv)
        var planDateTv: TextView = findViewById(R.id.plan_date_tv)

        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
        val current = LocalDateTime.now().format(formatter)

        val savedPlans = loadPlans()
        savedPlans.forEachIndexed { i, plan ->
            addPlanToLayout(linearLayout, plan, i + 1)
        }
        index = savedPlans.size + 1

        val datePickerDialogListener =
            DatePickerDialog.OnDateSetListener { view, year, monthOfYear, dayOfMonth ->
                val formattedDate = "${dayOfMonth}/${monthOfYear + 1}/$year"
                planDateTv.text = formattedDate
            }

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

        addBtn.setOnClickListener {
            val enterPlanText = enterPlan.text.toString()
            if (enterPlanText.isNotBlank()) {
                addPlanToLayout(linearLayout, enterPlanText, index)
                savePlan(enterPlanText)
                index++
                enterPlan.text.clear()
            } else {
                showDialog("Alert", "Enter a plan", "OK")
            }
        }

        delBtn.setOnClickListener {
            val childCount = linearLayout.childCount
            if (childCount > 0) {
                linearLayout.removeViewAt(childCount - 1)
                removeLastPlan()
                if (index > 1) index--
            } else {
                showDialog("Alert", "There are no plans to delete", "OK")
            }
        }

        backBtn.setOnClickListener {
            val intent = Intent(this, MainMenu::class.java)
            startActivity(intent)
        }
        */

        /*
        private fun addPlanToLayout(linearLayout: LinearLayout, plan: String, index: Int) {
            val newPlanText = TextView(this)
            newPlanText.text = "$index. $plan"
            newPlanText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 30f)
            newPlanText.typeface = Typeface.create("sans-serif", Typeface.NORMAL)

            newPlanText.alpha = 0f
            newPlanText.animate().alpha(1f).setDuration(200).start()

            linearLayout.addView(newPlanText)
        }

        private fun savePlan(plan: String) {
            val sharedPreferences = getSharedPreferences(sharedPreferencesName, Context.MODE_PRIVATE)
            val existingPlans = loadPlans().toMutableList()
            existingPlans.add(plan)
            sharedPreferences.edit().putString(plansKey, Gson().toJson(existingPlans)).apply()
        }

        private fun removeLastPlan() {
            val sharedPreferences = getSharedPreferences(sharedPreferencesName, Context.MODE_PRIVATE)
            val existingPlans = loadPlans().toMutableList()
            if (existingPlans.isNotEmpty()) {
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

        private fun showDialog(
            title: String,
            message: String,
            positiveButtonText: String = "OK",
            positiveButtonAction: (() -> Unit)? = null,
            negativeButtonText: String? = null,
            negativeButtonAction: (() -> Unit)? = null
        ) {
            val alertDialog = AlertDialog.Builder(this).setTitle(title).setMessage(message)
                .setPositiveButton(positiveButtonText) { dialog, _ ->
                    dialog.dismiss()
                    positiveButtonAction?.invoke()
                }
            if (negativeButtonText != null) {
                alertDialog.setNegativeButton(negativeButtonText) { dialog, _ ->
                    dialog.dismiss()
                    negativeButtonAction?.invoke()
                }
            }
            alertDialog.create().show()
        }
        */
    }
}
