package com.example.travel_organiser

import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.ViewCompat
import com.example.travel_organiser.databinding.ActivityMainMenuBinding
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class MainMenu : AppCompatActivity() {

    private lateinit var binding: ActivityMainMenuBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main_menu)

        binding = ActivityMainMenuBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val backgroundColor = resources.getColor(R.color.backgroundColor)

        // Matching the status bar and navigation bar colors with the background color
        window.navigationBarColor = backgroundColor
        window.statusBarColor = backgroundColor

        // Set up the toolbar as the support ActionBar
        setSupportActionBar(binding.toolbarTop)

        // Set the title
        supportActionBar?.title = "Welcome back!"

        // Load the custom font
        val customFont: Typeface? = ResourcesCompat.getFont(this, R.font.nunito_bolditalic)

        // Find the title TextView inside the Toolbar
        for (i in 0 until binding.toolbarTop.childCount) {
            val view = binding.toolbarTop.getChildAt(i)
            if (view is TextView && view.text == "Welcome back!") {
                // Apply the custom font to the Toolbar title
                view.typeface = customFont
                view.textSize = 32f
                break
            }
        }

        // Floating Action Button click listener
        binding.fab.setOnClickListener {
            val intent = Intent(this, PlanCreator::class.java)
            startActivity(intent)
        }
    }
}
