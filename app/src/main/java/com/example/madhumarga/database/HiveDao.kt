package com.example.madhumarga.database

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface HiveDao {
    @Query("SELECT * FROM hives ORDER BY id DESC")
    fun getAllHives(): LiveData<List<Hive>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHive(hive: Hive)

    @Delete
    suspend fun deleteHive(hive: Hive)
}