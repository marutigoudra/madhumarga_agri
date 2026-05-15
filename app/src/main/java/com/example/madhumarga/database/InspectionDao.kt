package com.example.madhumarga.database

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface InspectionDao {
    @Query("SELECT * FROM inspections ORDER BY date DESC")
    fun getAllInspections(): LiveData<List<Inspection>>

    @Query("SELECT * FROM inspections WHERE hiveId = :hiveId ORDER BY date DESC")
    fun getInspectionsForHive(hiveId: Int): LiveData<List<Inspection>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInspection(inspection: Inspection)

    // This is the key query for the GenAI alert feature
    @Query("SELECT * FROM inspections WHERE activityLevel = 'Low' ORDER BY date DESC")
    suspend fun getLowActivityInspections(): List<Inspection>
}