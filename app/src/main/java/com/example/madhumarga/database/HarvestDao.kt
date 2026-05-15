package com.example.madhumarga.database

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface HarvestDao {
    @Query("SELECT * FROM harvest_records ORDER BY date DESC")
    fun getAllHarvests(): LiveData<List<HarvestRecord>>

    // Year-over-year comparison query (success criteria)
    @Query("SELECT year, SUM(quantityKg) as total FROM harvest_records GROUP BY year ORDER BY year DESC")
    suspend fun getYearlyTotals(): List<YearlyTotal>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHarvest(record: HarvestRecord)
}

data class YearlyTotal(val year: Int, val total: Double)