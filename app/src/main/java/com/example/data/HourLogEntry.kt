package com.example.data

import androidx.room.Entity

@Entity(
    tableName = "hour_logs",
    primaryKeys = ["date", "hourIndex"]
)
data class HourLogEntry(
    val date: String,          // YYYY-MM-DD format
    val hourIndex: Int,        // 0 to 23
    val activity: String,      // Description of the hour
    val category: String,      // "productive", "wasting", "essential", "college", "sleep", "none"
    val mood: String = "😐"     // Mood indicator emoji: 🌟, 😊, 😐, 😔, 🥱
) {
    companion object {
        const val CATEGORY_PRODUCTIVE = "productive"
        const val CATEGORY_WASTING = "wasting"
        const val CATEGORY_ESSENTIAL = "essential"
        const val CATEGORY_COLLEGE = "college"
        const val CATEGORY_SLEEP = "sleep"
        const val CATEGORY_NONE = "none"

        val ALL_CATEGORIES = listOf(
            CATEGORY_PRODUCTIVE,
            CATEGORY_WASTING,
            CATEGORY_ESSENTIAL,
            CATEGORY_COLLEGE,
            CATEGORY_SLEEP,
            CATEGORY_NONE
        )
    }
}
