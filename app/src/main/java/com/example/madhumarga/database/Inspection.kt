package com.example.madhumarga.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "inspections")
data class Inspection(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val hiveId: Int,
    val hiveName: String,
    val queenSeen: Boolean,
    val pestsFound: Boolean,
    val activityLevel: String,   // "High", "Medium", "Low"
    val honeyFlow: String,       // "Good", "Average", "Poor"
    val notes: String,
    val date: String
)