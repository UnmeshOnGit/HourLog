package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface HourLogDao {

    @Query("SELECT * FROM hour_logs WHERE date = :date ORDER BY hourIndex ASC")
    fun getLogsForDate(date: String): Flow<List<HourLogEntry>>

    @Query("SELECT * FROM hour_logs WHERE date = :date ORDER BY hourIndex ASC")
    suspend fun getLogsForDateSync(date: String): List<HourLogEntry>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(entry: HourLogEntry)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLogs(entries: List<HourLogEntry>)

    @Query("SELECT * FROM hour_logs WHERE activity LIKE '%' || :query || '%' AND activity != '' ORDER BY date DESC, hourIndex ASC")
    fun searchLogs(query: String): Flow<List<HourLogEntry>>

    @Query("SELECT DISTINCT date FROM hour_logs WHERE activity != '' OR category != 'none'")
    fun getDaysWithLogs(): Flow<List<String>>

    @Query("SELECT * FROM hour_logs ORDER BY date DESC, hourIndex ASC")
    fun getAllLogs(): Flow<List<HourLogEntry>>

    @Query("SELECT * FROM hour_logs ORDER BY date DESC, hourIndex ASC")
    suspend fun getAllLogsSync(): List<HourLogEntry>

    @Query("DELETE FROM hour_logs")
    suspend fun clearAllLogs()
}
