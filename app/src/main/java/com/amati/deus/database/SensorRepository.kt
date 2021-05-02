package com.amati.deus.database

import androidx.annotation.WorkerThread
import com.amati.deus.models.DeviceSensor
import kotlinx.coroutines.flow.Flow

// Declares the DAO as a private property in the constructor. Pass in the DAO
// instead of the whole database, because you only need access to the DAO
class SensorRepository(private val deviceSensorDao: DeviceSensorDao) {

    // Room executes all queries on a separate thread.
    // Observed Flow will notify the observer when the data has changed.
    val allSensors: Flow<List<DeviceSensor>> = deviceSensorDao.getAvailableSensors()

    // By default Room runs suspend queries off the main thread, therefore, we don't need to
    // implement anything else to ensure we're not doing long running database work
    // off the main thread.
    @WorkerThread
    suspend fun insert(deviceSensor: DeviceSensor) {
        deviceSensorDao.insert(deviceSensor)
    }
}