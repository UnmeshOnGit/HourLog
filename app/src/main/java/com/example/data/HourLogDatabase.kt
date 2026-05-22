package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [HourLogEntry::class], version = 1, exportSchema = false)
abstract class HourLogDatabase : RoomDatabase() {

    abstract fun hourLogDao(): HourLogDao

    companion object {
        @Volatile
        private var INSTANCE: HourLogDatabase? = null

        fun getDatabase(context: Context): HourLogDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    HourLogDatabase::class.java,
                    "hourlog_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
