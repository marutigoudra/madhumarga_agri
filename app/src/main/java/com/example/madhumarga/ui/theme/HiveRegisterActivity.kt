package com.example.madhumarga.ui

import android.os.Bundle
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.madhumarga.R
import com.example.madhumarga.database.AppDatabase
import com.example.madhumarga.database.Hive
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class HiveRegisterActivity : AppCompatActivity() {

    private lateinit var db: AppDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_hive_register)
        supportActionBar?.title = "Hive Register"

        db = AppDatabase.getDatabase(this)

        val etName = findViewById<TextInputEditText>(R.id.etHiveName)
        val etLocation = findViewById<TextInputEditText>(R.id.etLocation)
        val btnSave = findViewById<Button>(R.id.btnSaveHive)

        btnSave.setOnClickListener {
            val name = etName.text.toString().trim()
            val location = etLocation.text.toString().trim()
            if (name.isEmpty() || location.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
            val hive = Hive(name = name, location = location, createdDate = today)
            lifecycleScope.launch {
                db.hiveDao().insertHive(hive)
                runOnUiThread {
                    Toast.makeText(this@HiveRegisterActivity, "Hive saved!", Toast.LENGTH_SHORT).show()
                    etName.text?.clear()
                    etLocation.text?.clear()
                }
            }
        }

        // Show all hives in list
        db.hiveDao().getAllHives().observe(this) { hives ->
            val container = findViewById<LinearLayout>(R.id.hiveListContainer)
            container.removeAllViews()
            hives.forEach { hive ->
                val tv = TextView(this)
                tv.text = "🐝 ${hive.name}  |  ${hive.location}  |  Added: ${hive.createdDate}"
                tv.textSize = 14f
                tv.setPadding(0, 10, 0, 10)
                tv.setTextColor(resources.getColor(R.color.text_dark, null))
                container.addView(tv)
            }
        }
    }
}