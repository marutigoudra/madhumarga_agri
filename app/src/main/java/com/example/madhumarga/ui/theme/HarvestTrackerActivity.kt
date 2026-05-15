package com.example.madhumarga.ui

import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.madhumarga.R
import com.example.madhumarga.database.AppDatabase
import com.example.madhumarga.database.HarvestRecord
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class HarvestTrackerActivity : AppCompatActivity() {

    private lateinit var db: AppDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_harvest_tracker)
        supportActionBar?.title = "Harvest Tracker"

        db = AppDatabase.getDatabase(this)

        val etHiveName = findViewById<TextInputEditText>(R.id.etHiveName)
        val etQuantity = findViewById<TextInputEditText>(R.id.etQuantity)
        val etNotes    = findViewById<TextInputEditText>(R.id.etNotes)
        val btnSave    = findViewById<Button>(R.id.btnSaveHarvest)

        btnSave.setOnClickListener {
            val hiveName = etHiveName.text.toString().trim()
            val qtyStr   = etQuantity.text.toString().trim()
            if (hiveName.isEmpty() || qtyStr.isEmpty()) {
                Toast.makeText(this, "Please fill hive name and quantity", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val qty   = qtyStr.toDoubleOrNull() ?: 0.0
            val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
            val year  = Calendar.getInstance().get(Calendar.YEAR)

            val record = HarvestRecord(
                hiveId     = 0,
                hiveName   = hiveName,
                quantityKg = qty,
                year       = year,
                date       = today,
                notes      = etNotes.text.toString().trim()
            )

            lifecycleScope.launch {
                db.harvestDao().insertHarvest(record)
                runOnUiThread {
                    Toast.makeText(this@HarvestTrackerActivity, "Harvest saved!", Toast.LENGTH_SHORT).show()
                    etHiveName.text?.clear()
                    etQuantity.text?.clear()
                    etNotes.text?.clear()
                    loadYearlyComparison()
                }
            }
        }

        // Observe and show all harvests
        db.harvestDao().getAllHarvests().observe(this) { records ->
            val container = findViewById<LinearLayout>(R.id.harvestListContainer)
            container.removeAllViews()
            records.forEach { rec ->
                val tv = TextView(this)
                tv.text = "🍯 ${rec.hiveName}  —  ${rec.quantityKg} kg  |  ${rec.date}"
                tv.textSize = 14f
                tv.setPadding(0, 10, 0, 10)
                tv.setTextColor(resources.getColor(R.color.text_dark, null))
                container.addView(tv)

                val divider = View(this)
                divider.layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, 1
                )
                divider.setBackgroundColor(resources.getColor(R.color.honey_light, null))
                container.addView(divider)
            }
        }

        // Set a fun progress bar (shows harvest season progress based on current month)
        val month = Calendar.getInstance().get(Calendar.MONTH) + 1  // 1–12
        // Peak honey season in India: March–July (months 3–7)
        val progress = when (month) {
            in 3..7  -> ((month - 2) * 20).coerceIn(0, 100)
            in 8..12 -> 100
            else     -> 10
        }
        findViewById<ProgressBar>(R.id.progressHoneyFlow).progress = progress
        findViewById<TextView>(R.id.tvFlowProgress).text =
            "Honey Flow Season Progress — $progress% of season complete"

        loadYearlyComparison()
    }

    // Shows year-over-year totals as simple text rows with a small bar
    private fun loadYearlyComparison() {
        lifecycleScope.launch {
            val totals = db.harvestDao().getYearlyTotals()
            val container = findViewById<LinearLayout>(R.id.yearlyComparisonContainer)
            runOnUiThread {
                container.removeAllViews()
                if (totals.isEmpty()) {
                    val tv = TextView(this@HarvestTrackerActivity)
                    tv.text = "No harvest data yet."
                    tv.textSize = 13f
                    tv.setTextColor(resources.getColor(R.color.text_gray, null))
                    container.addView(tv)
                    return@runOnUiThread
                }

                val maxTotal = totals.maxOf { it.total }.takeIf { it > 0 } ?: 1.0

                totals.forEach { yt ->
                    // Year label
                    val header = TextView(this@HarvestTrackerActivity)
                    header.text = "📅 ${yt.year}  —  ${"%.2f".format(yt.total)} kg total"
                    header.textSize = 14f
                    header.setTextColor(resources.getColor(R.color.text_dark, null))
                    header.setPadding(0, 8, 0, 4)
                    container.addView(header)

                    // Mini progress bar as visual comparison
                    val bar = ProgressBar(
                        this@HarvestTrackerActivity,
                        null,
                        android.R.attr.progressBarStyleHorizontal
                    )
                    bar.layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT, 20
                    ).also { it.bottomMargin = 8 }
                    bar.max = 100
                    bar.progress = ((yt.total / maxTotal) * 100).toInt()
                    bar.progressTintList = resources.getColorStateList(R.color.honey_primary, null)
                    container.addView(bar)
                }
            }
        }
    }
}