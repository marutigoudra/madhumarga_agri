package com.example.madhumarga.ui

import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.madhumarga.R
import com.example.madhumarga.database.AppDatabase
import com.example.madhumarga.database.Inspection
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*

class InspectionLogActivity : AppCompatActivity() {

    private lateinit var db: AppDatabase

    // ✅ PASTE YOUR GROQ API KEY BELOW (replace the text inside the quotes)
    private val GROQ_API_KEY = "paste_your_groq_key_here"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_inspection_log)
        supportActionBar?.title = "Inspection Log"

        db = AppDatabase.getDatabase(this)

        val etHiveName = findViewById<TextInputEditText>(R.id.etHiveName)
        val cbQueen    = findViewById<CheckBox>(R.id.cbQueenSeen)
        val cbPests    = findViewById<CheckBox>(R.id.cbPestsFound)
        val rgActivity = findViewById<RadioGroup>(R.id.rgActivityLevel)
        val rgFlow     = findViewById<RadioGroup>(R.id.rgHoneyFlow)
        val etNotes    = findViewById<TextInputEditText>(R.id.etNotes)
        val btnSave    = findViewById<Button>(R.id.btnSaveInspection)
        val aiBox      = findViewById<LinearLayout>(R.id.aiAdviceBox)
        val tvAiAdvice = findViewById<TextView>(R.id.tvAiAdvice)

        btnSave.setOnClickListener {
            val hiveName = etHiveName.text.toString().trim()
            if (hiveName.isEmpty()) {
                Toast.makeText(this, "Please enter hive name", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val activityLevel = when (rgActivity.checkedRadioButtonId) {
                R.id.rbHigh   -> "High"
                R.id.rbMedium -> "Medium"
                R.id.rbLow    -> "Low"
                else          -> "Medium"
            }

            val honeyFlow = when (rgFlow.checkedRadioButtonId) {
                R.id.rbFlowGood    -> "Good"
                R.id.rbFlowAverage -> "Average"
                R.id.rbFlowPoor    -> "Poor"
                else               -> "Average"
            }

            val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
            val inspection = Inspection(
                hiveId        = 0,
                hiveName      = hiveName,
                queenSeen     = cbQueen.isChecked,
                pestsFound    = cbPests.isChecked,
                activityLevel = activityLevel,
                honeyFlow     = honeyFlow,
                notes         = etNotes.text.toString().trim(),
                date          = today
            )

            lifecycleScope.launch {
                // Save to Room database
                db.inspectionDao().insertInspection(inspection)

                // Show loading
                withContext(Dispatchers.Main) {
                    aiBox.visibility  = View.VISIBLE
                    tvAiAdvice.text   = "Getting AI advice..."
                    btnSave.isEnabled = false
                }

                // Call Groq AI
                val advice = getGroqAdvice(inspection)

                // Show result
                withContext(Dispatchers.Main) {
                    tvAiAdvice.text   = advice
                    btnSave.isEnabled = true
                    Toast.makeText(
                        this@InspectionLogActivity,
                        "Inspection saved!",
                        Toast.LENGTH_SHORT
                    ).show()
                    etHiveName.text?.clear()
                    etNotes.text?.clear()
                    cbQueen.isChecked = false
                    cbPests.isChecked = false
                    rgActivity.clearCheck()
                    rgFlow.clearCheck()
                }
            }
        }

        // Show past inspections
        db.inspectionDao().getAllInspections().observe(this) { inspections ->
            val container = findViewById<LinearLayout>(R.id.inspectionListContainer)
            container.removeAllViews()
            inspections.forEach { insp ->
                val queenIcon = if (insp.queenSeen) "Yes" else "No"
                val pestIcon  = if (insp.pestsFound) "Yes" else "No"
                val tv = TextView(this)
                tv.text = """
                    Hive: ${insp.hiveName}  |  Date: ${insp.date}
                    Queen Seen: $queenIcon  |  Pests: $pestIcon
                    Activity: ${insp.activityLevel}  |  Flow: ${insp.honeyFlow}
                    Notes: ${insp.notes.ifEmpty { "None" }}
                """.trimIndent()
                tv.textSize = 13f
                tv.setTextColor(resources.getColor(R.color.text_dark, null))
                tv.setPadding(0, 12, 0, 12)

                if (insp.activityLevel == "Low") {
                    tv.setBackgroundColor(0xFFFFCDD2.toInt())
                }

                container.addView(tv)

                val divider = View(this)
                divider.layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, 1
                )
                divider.setBackgroundColor(resources.getColor(R.color.honey_light, null))
                container.addView(divider)
            }
        }
    }

    private suspend fun getGroqAdvice(insp: Inspection): String = withContext(Dispatchers.IO) {
        try {
            val client = OkHttpClient()

            val prompt = """
                You are an expert beekeeper assistant. A farmer logged this hive inspection:
                - Hive: ${insp.hiveName}
                - Queen seen: ${insp.queenSeen}
                - Pests found: ${insp.pestsFound}
                - Activity level: ${insp.activityLevel}
                - Honey flow: ${insp.honeyFlow}
                - Notes: ${insp.notes}
                
                Give 2 to 3 short practical tips for what the farmer should do next.
                If activity is Low, clearly say INTERVENTION ALERT at the start.
                Keep response under 80 words. Use simple language.
            """.trimIndent()

            // Build request body in Groq format (OpenAI-compatible)
            val requestBody = JSONObject().apply {
                put("model", "llama3-8b-8192")   // free Groq model
                put("max_tokens", 300)
                put("messages", JSONArray().apply {
                    put(JSONObject().apply {
                        put("role", "user")
                        put("content", prompt)
                    })
                })
            }

            val request = Request.Builder()
                .url("https://api.groq.com/openai/v1/chat/completions")
                .addHeader("Authorization", "Bearer $GROQ_API_KEY")
                .addHeader("Content-Type", "application/json")
                .post(
                    requestBody.toString()
                        .toRequestBody("application/json".toMediaType())
                )
                .build()

            val response = client.newCall(request).execute()
            val json     = JSONObject(response.body?.string() ?: "")

            // Groq response format: choices[0].message.content
            json.getJSONArray("choices")
                .getJSONObject(0)
                .getJSONObject("message")
                .getString("content")

        } catch (e: Exception) {
            "Could not get AI advice. Check your internet connection.\nError: ${e.message}"
        }
    }
}