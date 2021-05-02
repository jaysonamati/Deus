package com.amati.deus.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.amati.deus.models.DeviceSensor
import kotlinx.coroutines.flow.Flow


@Dao
interface DeviceSensorDao {

    @Query("SELECT * FROM sensor_table ORDER BY sensorName ASC")
    fun getAvailableSensors(): Flow<List<DeviceSensor>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(deviceSensor: DeviceSensor)

    @Query("DELETE FROM sensor_table")
    suspend fun deleteAll()
}