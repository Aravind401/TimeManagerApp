package com.nubiq.timemanagerapp.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.nubiq.timemanagerapp.data.database.dao.CategoryDao
import com.nubiq.timemanagerapp.data.database.dao.TimeActivityDao
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [TimeActivity::class, Category::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun timeActivityDao(): TimeActivityDao
    abstract fun categoryDao(): CategoryDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "time_manager_db"  // Changed database name
                )
                    .addCallback(DatabaseCallback())
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                // Insert default categories in background
                CoroutineScope(Dispatchers.IO).launch {
                    // Note: We can't access database here directly
                    // We'll handle this differently
                }
            }
        }
    }
}