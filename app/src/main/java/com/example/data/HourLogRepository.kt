package com.example.data

import kotlinx.coroutines.flow.Flow

class HourLogRepository(private val hourLogDao: HourLogDao) {

    fun getLogsForDate(date: String): Flow<List<HourLogEntry>> =
        hourLogDao.getLogsForDate(date)

    suspend fun getLogsForDateSync(date: String): List<HourLogEntry> =
        hourLogDao.getLogsForDateSync(date)

    suspend fun insertLog(entry: HourLogEntry) =
        hourLogDao.insertLog(entry)

    suspend fun insertLogs(entries: List<HourLogEntry>) =
        hourLogDao.insertLogs(entries)

    fun searchLogs(query: String): Flow<List<HourLogEntry>> =
        hourLogDao.searchLogs(query)

    fun getDaysWithLogs(): Flow<List<String>> =
        hourLogDao.getDaysWithLogs()

    fun getAllLogs(): Flow<List<HourLogEntry>> =
        hourLogDao.getAllLogs()

    suspend fun getAllLogsSync(): List<HourLogEntry> =
        hourLogDao.getAllLogsSync()

    suspend fun clearAllLogs() =
        hourLogDao.clearAllLogs()
}
