package com.example.data

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

object DateUtils {
    private val dbFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
    private val displayFormat = SimpleDateFormat("EEEE, MMMM d, yyyy", Locale.US)
    private val shortDisplayFormat = SimpleDateFormat("MMM d, yyyy", Locale.US)
    private val monthYearFormat = SimpleDateFormat("MMMM yyyy", Locale.US)
    private val dayNameFormat = SimpleDateFormat("EEEE", Locale.US)

    fun getTodayDateString(): String {
        return dbFormat.format(Date())
    }

    fun parseDate(dateStr: String): Date {
        return try {
            dbFormat.parse(dateStr) ?: Date()
        } catch (e: Exception) {
            Date()
        }
    }

    fun formatToDisplay(dateStr: String): String {
        return try {
            val date = dbFormat.parse(dateStr) ?: return dateStr
            displayFormat.format(date)
        } catch (e: Exception) {
            dateStr
        }
    }

    fun formatToShortDisplay(dateStr: String): String {
        return try {
            val date = dbFormat.parse(dateStr) ?: return dateStr
            shortDisplayFormat.format(date)
        } catch (e: Exception) {
            dateStr
        }
    }

    fun getDayName(dateStr: String): String {
        return try {
            val date = dbFormat.parse(dateStr) ?: return ""
            dayNameFormat.format(date)
        } catch (e: Exception) {
            ""
        }
    }

    fun formatMonthYear(calendar: Calendar): String {
        return monthYearFormat.format(calendar.time)
    }

    fun getOffsetDateString(dateStr: String, offsetDays: Int): String {
        return try {
            val date = dbFormat.parse(dateStr) ?: return dateStr
            val cal = Calendar.getInstance()
            cal.time = date
            cal.add(Calendar.DAY_OF_YEAR, offsetDays)
            dbFormat.format(cal.time)
        } catch (e: Exception) {
            dateStr
        }
    }

    fun getHourLabel(hourIndex: Int): String {
        return when (hourIndex) {
            0 -> "12:00 AM - 01:00 AM"
            12 -> "12:00 PM - 01:00 PM"
            else -> {
                val isPm = hourIndex >= 12
                val currentHour = if (isPm) hourIndex - 12 else hourIndex
                val nextHour = currentHour + 1
                
                val currentHourStr = String.format("%02d", currentHour)
                val nextHourStr = String.format("%02d", nextHour)
                val period = if (isPm) "PM" else "AM"
                
                // Special periods crossover logic
                if (hourIndex == 11) {
                    "11:00 AM - 12:00 PM"
                } else if (hourIndex == 23) {
                    "11:00 PM - 12:00 AM"
                } else {
                    "$currentHourStr:00 $period - $nextHourStr:00 $period"
                }
            }
        }
    }
}
