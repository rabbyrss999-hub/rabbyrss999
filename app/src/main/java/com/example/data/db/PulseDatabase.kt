package com.example.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.model.QuickNote
import com.example.data.model.TaskItem
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(entities = [TaskItem::class, QuickNote::class], version = 1, exportSchema = false)
abstract class PulseDatabase : RoomDatabase() {
    abstract fun pulseDao(): PulseDao

    companion object {
        @Volatile
        private var INSTANCE: PulseDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): PulseDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    PulseDatabase::class.java,
                    "mobile_pulse_database"
                )
                    .addCallback(PulseDatabaseCallback(scope))
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }

    private class PulseDatabaseCallback(
        private val scope: CoroutineScope
    ) : RoomDatabase.Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            INSTANCE?.let { database ->
                scope.launch(Dispatchers.IO) {
                    populateInitialData(database.pulseDao())
                }
            }
        }

        private suspend fun populateInitialData(dao: PulseDao) {
            val initialTasks = listOf(
                TaskItem(
                    title = "Check Battery Health & Temp",
                    description = "Verify operating temperature is below 38°C and voltage is stable",
                    category = "Battery",
                    priority = "High",
                    isCompleted = false
                ),
                TaskItem(
                    title = "Calibrate Digital Bubble Level",
                    description = "Use the hardware accelerometer tool on a flat surface to test orientation sensors",
                    category = "Hardware",
                    priority = "Medium",
                    isCompleted = false
                ),
                TaskItem(
                    title = "Run Display Pixel Purity Test",
                    description = "Inspect screen color uniformity and check for stuck pixels",
                    category = "Mobile",
                    priority = "Low",
                    isCompleted = false
                ),
                TaskItem(
                    title = "Review Storage Free Space",
                    description = "Ensure system has at least 15% free internal storage for smooth caching",
                    category = "Storage",
                    priority = "Medium",
                    isCompleted = true,
                    completedAt = System.currentTimeMillis()
                ),
                TaskItem(
                    title = "Test Haptic Motor Feedback",
                    description = "Run vibration patterns (tick, double-click, heartbeat) to confirm linear motor health",
                    category = "Hardware",
                    priority = "Low",
                    isCompleted = false
                )
            )
            dao.insertTasks(initialTasks)

            val initialNotes = listOf(
                QuickNote(
                    title = "Device Maintenance Tips",
                    content = "Keep device battery between 20% and 80% to maximize lithium-ion cycle lifespan. Avoid intense gaming while rapid charging.",
                    tag = "Battery",
                    colorHex = "#06B6D4"
                ),
                QuickNote(
                    title = "Wi-Fi & Network Performance",
                    content = "If mobile network speeds drop, toggle Airplane mode on for 5 seconds to force modern cell tower handshakes.",
                    tag = "Mobile",
                    colorHex = "#10B981"
                ),
                QuickNote(
                    title = "Sensor Suite Calibration",
                    content = "Figure-8 motion recalibrates the compass magnetometer, while laying flat resets the accelerometer 0-point.",
                    tag = "Hardware",
                    colorHex = "#8B5CF6"
                )
            )
            dao.insertNotes(initialNotes)
        }
    }
}
