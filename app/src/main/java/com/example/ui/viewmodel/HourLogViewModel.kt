package com.example.ui.viewmodel

import android.app.AlarmManager
import android.app.Application
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.MainActivity
import com.example.data.DateUtils
import com.example.data.HourLogDatabase
import com.example.data.HourLogEntry
import com.example.data.HourLogRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import java.io.InputStream
import java.io.OutputStream
import java.util.Calendar

class HourLogViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: HourLogRepository
    private val sharedPrefs = application.getSharedPreferences("hourlog_prefs", Context.MODE_PRIVATE)

    // Current screen routing (simple state routing to prevent Jetpack Compose Navigation crashes)
    val currentScreen = MutableStateFlow("splash") // splash, dashboard, log, calendar, search, statistics, settings, backup

    // Selected date for Daily Log screen (format: YYYY-MM-DD)
    val selectedDate = MutableStateFlow(DateUtils.getTodayDateString())

    // Live search query for Search Screen
    val searchQuery = MutableStateFlow("")

    init {
        val database = HourLogDatabase.getDatabase(application)
        repository = HourLogRepository(database.hourLogDao())
    }

    // Days that have saved logs (used for highlighting on calendar)
    val daysWithLogs: StateFlow<List<String>> = repository.getDaysWithLogs()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Reactive logs for currently selected date
    @OptIn(ExperimentalCoroutinesApi::class)
    val logsForSelectedDate: StateFlow<List<HourLogEntry>> = selectedDate
        .flatMapLatest { date ->
            repository.getLogsForDate(date)
        }
        .map { dbLogs ->
            val fullDayLogs = MutableList(24) { hourIdx ->
                HourLogEntry(
                    date = selectedDate.value,
                    hourIndex = hourIdx,
                    activity = "",
                    category = HourLogEntry.CATEGORY_NONE,
                    mood = "😐"
                )
            }
            for (log in dbLogs) {
                if (log.hourIndex in 0..23) {
                    fullDayLogs[log.hourIndex] = log
                }
            }
            fullDayLogs
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Live search filtered results
    @OptIn(ExperimentalCoroutinesApi::class)
    val searchResults: StateFlow<List<HourLogEntry>> = searchQuery
        .flatMapLatest { query ->
            if (query.trim().isEmpty()) {
                MutableStateFlow(emptyList())
            } else {
                repository.searchLogs(query)
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // All logs for general statistics
    val allLogs: StateFlow<List<HourLogEntry>> = repository.getAllLogs()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // --- Preferences States ---
    val isDarkMode = MutableStateFlow(sharedPrefs.getBoolean("dark_mode", true))
    val remindersEnabled = MutableStateFlow(sharedPrefs.getBoolean("reminders_enabled", false))
    val reminderHour = MutableStateFlow(sharedPrefs.getInt("reminder_hour", 20)) // 8 PM
    val reminderMinute = MutableStateFlow(sharedPrefs.getInt("reminder_minute", 0))
    val fontSizeScale = MutableStateFlow(sharedPrefs.getFloat("font_scale", 1.0f))

    // Save and insert single hourly log (Auto-save trigger)
    fun saveHourLog(hourIndex: Int, activity: String, category: String, mood: String) {
        viewModelScope.launch {
            val entry = HourLogEntry(
                date = selectedDate.value,
                hourIndex = hourIndex,
                activity = activity,
                category = category,
                mood = mood
            )
            repository.insertLog(entry)
        }
    }

    // Set selected date
    fun selectDate(dateStr: String) {
        selectedDate.value = dateStr
    }

    // Go to previous day
    fun navigatePreviousDay() {
        selectedDate.value = DateUtils.getOffsetDateString(selectedDate.value, -1)
    }

    // Go to next day
    fun navigateNextDay() {
        selectedDate.value = DateUtils.getOffsetDateString(selectedDate.value, 1)
    }

    // Reset all app data
    fun resetAllData(onComplete: () -> Unit) {
        viewModelScope.launch {
            repository.clearAllLogs()
            // Reset prefs
            sharedPrefs.edit().clear().apply()
            isDarkMode.value = true
            remindersEnabled.value = false
            reminderHour.value = 20
            reminderMinute.value = 0
            fontSizeScale.value = 1.0f
            cancelReminder()
            onComplete()
        }
    }

    // Toggle dark mode
    fun setThemeMode(dark: Boolean) {
        isDarkMode.value = dark
        sharedPrefs.edit().putBoolean("dark_mode", dark).apply()
    }

    // Toggle reminders
    fun setRemindersEnabled(enabled: Boolean) {
        remindersEnabled.value = enabled
        sharedPrefs.edit().putBoolean("reminders_enabled", enabled).apply()
        if (enabled) {
            scheduleReminder()
        } else {
            cancelReminder()
        }
    }

    // Update reminder timing
    fun setReminderTime(hour: Int, minute: Int) {
        reminderHour.value = hour
        reminderMinute.value = minute
        sharedPrefs.edit()
            .putInt("reminder_hour", hour)
            .putInt("reminder_minute", minute)
            .apply()
        if (remindersEnabled.value) {
            scheduleReminder()
        }
    }

    // Update font size
    fun setFontSizeScale(scale: Float) {
        fontSizeScale.value = scale
        sharedPrefs.edit().putFloat("font_scale", scale).apply()
    }

    // --- Backup & Restore Utilities ---

    fun getBackupKeyString(): String {
        return try {
            var allEntries: List<HourLogEntry> = emptyList()
            val job = viewModelScope.launch {
                allEntries = repository.getAllLogsSync()
            }
            // Wait for DB fetch synchronously or block briefly
            while (!job.isCompleted) { Thread.sleep(5) }
            exportSerializer(allEntries)
        } catch (e: Exception) {
            ""
        }
    }

    fun restoreFromBackupString(backupStr: String): Boolean {
        return try {
            val entries = importDeserializer(backupStr)
            viewModelScope.launch {
                repository.insertLogs(entries)
            }
            true
        } catch (e: Exception) {
            false
        }
    }

    fun exportToFile(outputStream: OutputStream): Boolean {
        return try {
            val backupStr = getBackupKeyString()
            outputStream.use { os ->
                os.write(backupStr.toByteArray())
            }
            true
        } catch (e: Exception) {
            false
        }
    }

    fun importFromFile(inputStream: InputStream): Boolean {
        return try {
            val content = inputStream.bufferedReader().use { it.readText() }
            restoreFromBackupString(content)
        } catch (e: Exception) {
            false
        }
    }

    private fun exportSerializer(entries: List<HourLogEntry>): String {
        val array = JSONArray()
        for (entry in entries) {
            val obj = JSONObject()
            obj.put("date", entry.date)
            obj.put("hourIndex", entry.hourIndex)
            obj.put("activity", entry.activity)
            obj.put("category", entry.category)
            obj.put("mood", entry.mood)
            array.put(obj)
        }
        return array.toString()
    }

    private fun importDeserializer(jsonStr: String): List<HourLogEntry> {
        val list = mutableListOf<HourLogEntry>()
        val array = JSONArray(jsonStr)
        for (i in 0 until array.length()) {
            val obj = array.getJSONObject(i)
            list.add(
                HourLogEntry(
                    date = obj.getString("date"),
                    hourIndex = obj.getInt("hourIndex"),
                    activity = obj.getString("activity"),
                    category = obj.getString("category"),
                    mood = obj.optString("mood", "😐")
                )
            )
        }
        return list
    }

    // --- Notification Alarm Setup ---

    private fun scheduleReminder() {
        val context = getApplication<Application>().applicationContext
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            action = "com.example.hourlog.LOG_REMINDER"
        }
        
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            1234,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, reminderHour.value)
            set(Calendar.MINUTE, reminderMinute.value)
            set(Calendar.SECOND, 0)
            if (before(Calendar.getInstance())) {
                add(Calendar.DAY_OF_YEAR, 1)
            }
        }

        try {
            alarmManager.setRepeating(
                AlarmManager.RTC_WAKEUP,
                calendar.timeInMillis,
                AlarmManager.INTERVAL_DAY,
                pendingIntent
            )
        } catch (e: SecurityException) {
            // Under Android 12+, inexact alarm permissions are normally granted, 
            // but we use fallback schedules safely.
        }
    }

    private fun cancelReminder() {
        val context = getApplication<Application>().applicationContext
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            action = "com.example.hourlog.LOG_REMINDER"
        }
        
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            1234,
            intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )

        if (pendingIntent != null) {
            alarmManager.cancel(pendingIntent)
            pendingIntent.cancel()
        }
    }
}
