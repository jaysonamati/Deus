package com.amati.deus

import android.app.Application
import com.amati.deus.database.AppRoomDatabase
import com.amati.deus.database.SensorRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import timber.log.Timber

class DeusApplication: Application() {

    val applicationScope = CoroutineScope(SupervisorJob())

    // Using by lazy so the database and the repository are only created when they're needed
    // rather than when the application starts

    val database by lazy { AppRoomDatabase.getDataBase(this, applicationScope) }
    val repository by lazy { SensorRepository(database.deviceSensorDao()) }

    override fun onCreate() {
        super.onCreate()

        Timber.plant(Timber.DebugTree())
    }
}