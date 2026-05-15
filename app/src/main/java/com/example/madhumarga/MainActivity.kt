package com.example.madhumarga

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.madhumarga.database.AppDatabase
import com.example.madhumarga.ui.FloraCalendarActivity
import com.example.madhumarga.ui.HarvestTrackerActivity
import com.example.madhumarga.ui.HiveRegisterActivity
import com.example.madhumarga.ui.InspectionLogActivity
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var db: AppDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        db = AppDatabase.getDatabase(this)

        // Navigation buttons
        findViewById<LinearLayout>(R.id.btnHiveRegister).setOnClickListener {
            startActivity(Intent(this, HiveRegisterActivity::class.java))
        }
        findViewById<LinearLayout>(R.id.btnInspectionLog).setOnClickListener {
            startActivity(Intent(this, InspectionLogActivity::class.java))
        }
        findViewById<LinearLayout>(R.id.btnHarvestTracker).setOnClickListener {
            startActivity(Intent(this, HarvestTrackerActivity::class.java))
        }
        findViewById<LinearLayout>(R.id.btnFloraCalendar).setOnClickListener {
            startActivity(Intent(this, FloraCalendarActivity::class.java))
        }

        // Observe hives and display them
        db.hiveDao().getAllHives().observe(this) { hives ->
            val container = findViewById<LinearLayout>(R.id.hiveListContainer)
            container.removeAllViews()
            hives.forEach { hive ->
                val tv = TextView(this)
                tv.text = "🐝 ${hive.name}  —  ${hive.location}"
                tv.textSize = 15f
                tv.setPadding(0, 8, 0, 8)
                tv.setTextColor(resources.getColor(R.color.text_dark, null))
                container.addView(tv)
            }
        }

        // Check for Low Activity alert (the GenAI trigger)
        checkForAlert()
    }

    override fun onResume() {
        super.onResume()
        checkForAlert()
    }

    private fun checkForAlert() {
        lifecycleScope.launch {
            val lowActivity = db.inspectionDao().getLowActivityInspections()
            val banner = findViewById<LinearLayout>(R.id.alertBanner)
            val alertText = findViewById<TextView>(R.id.alertText)
            if (lowActivity.isNotEmpty()) {
                banner.visibility = View.VISIBLE
                alertText.text = "⚠️ Intervention Alert: ${lowActivity.size} hive(s) have Low Activity! Check them soon."
            } else {
                banner.visibility = View.GONE
            }
        }
    }
}