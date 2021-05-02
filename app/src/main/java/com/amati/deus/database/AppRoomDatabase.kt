package com.amati.deus.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.amati.deus.models.DeviceSensor
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch


@Database(entities = arrayOf(DeviceSensor::class), version = 1, exportSchema = false)
public abstract class AppRoomDatabase : RoomDatabase() {

    abstract fun deviceSensorDao(): DeviceSensorDao

    private class AppDatabaseCallback(private val scope: CoroutineScope) : RoomDatabase.Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            INSTANCE?.let { database ->
                scope.launch {
                    populateDatabase(database.deviceSensorDao())
                }
            }
        }

        suspend fun populateDatabase(deviceSensorDao: DeviceSensorDao) {
            // Delete all content here
            deviceSensorDao.deleteAll()

            // Add sample Sensor
            var sensor = DeviceSensor(0, "Sample Sensor")
            deviceSensorDao.insert(sensor)
            sensor = DeviceSensor(1, "Sample Sensor 2")
            deviceSensorDao.insert(sensor)
        }

    }

    companion object {
        // Singleton prevents multiple instances of database opening at the
        // same time.
        @Volatile
        private var INSTANCE: AppRoomDatabase? = null

        fun getDataBase(context: Context, scope: CoroutineScope): AppRoomDatabase {
            // if the INSTANCE is not null, then return it,
            // if it is, then create the database
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppRoomDatabase::class.java,
                    "app_database"
                )
                    .addCallback(AppDatabaseCallback(scope))
                    .build()
                INSTANCE = instance
                // return instance
                instance
            }
        }
    }
}