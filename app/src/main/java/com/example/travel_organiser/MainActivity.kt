package com.example.travel_organiser

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.TypedValue
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class MainActivity : AppCompatActivity() {
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

        add_btn.setOnClickListener {
            val enterPlanText = enterPlan.text.toString()
            val newPlanText = TextView(this)
            newPlanText.text = "$index. $enterPlanText"
            newPlanText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18f)
            linearLayout.addView(newPlanText)
            index++
        }

        del_btn.setOnClickListener {
            val childCount = linearLayout.childCount
            if (childCount > 0) {
                linearLayout.removeViewAt(childCount - 1)
                if (index > 1) index--
            }
        }
    }
}
