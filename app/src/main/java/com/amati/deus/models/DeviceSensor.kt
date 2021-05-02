package com.amati.deus.models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "sensor_table")
class DeviceSensor(@PrimaryKey(autoGenerate = false) val id: Int, val sensorName: String)