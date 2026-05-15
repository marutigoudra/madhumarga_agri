package com.example.madhumarga.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "harvest_records")
data class HarvestRecord(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val hiveId: Int,
    val hiveName: String,
    val quantityKg: Double,
    val year: Int,
    val date: String,
    val notes: String
)