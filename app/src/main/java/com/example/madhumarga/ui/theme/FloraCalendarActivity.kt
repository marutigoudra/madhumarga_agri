package com.example.madhumarga.ui

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.madhumarga.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.*

class FloraCalendarActivity : AppCompatActivity() {

    // ✅ PASTE YOUR GROQ API KEY BELOW (same key as InspectionLogActivity)
    private val GROQ_API_KEY = "paste_your_groq_key_here"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_flora_calendar)
        supportActionBar?.title = "Flora Calendar"

        val btnAsk     = findViewById<Button>(R.id.btnAskAiFlora)
        val aiBox      = findViewById<LinearLayout>(R.id.floraAiBox)
        val tvResponse = findViewById<TextView>(R.id.tvFloraAiResponse)

        btnAsk.setOnClickListener {
            aiBox.visibility  = View.VISIBLE
            tvResponse.text   = "Getting blooming info from AI..."
            btnAsk.isEnabled  = false

            // Get current month name
            val monthName = java.text.DateFormatSymbols()
                .months[Calendar.getInstance().get(Calendar.MONTH)]

            lifecycleScope.launch {
                val advice = getFloraAdvice(monthName)
                withContext(Dispatchers.Main) {
                    tvResponse.text  = advice
                    btnAsk.isEnabled = true
                }
            }
        }
    }

    private suspend fun getFloraAdvice(month: String): String = withContext(Dispatchers.IO) {
        try {
            val client = OkHttpClient()

            val prompt = """
                You are a beekeeper assistant in India.
                Tell me which flowers are blooming in $month in India that are good for honey bees.
                List 4 to 5 flowers with a short tip for each.
                Keep it under 120 words. Use simple language.
            """.trimIndent()

            val requestBody = JSONObject().apply {
                put("model", "llama3-8b-8192")
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

            json.getJSONArray("choices")
                .getJSONObject(0)
                .getJSONObject("message")
                .getString("content")

        } catch (e: Exception) {
            "Could not reach AI. Check your internet.\nError: ${e.message}"
        }
    }
}