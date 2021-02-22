package com.amati.deus

import android.app.Application
import timber.log.Timber

class DeusApplication: Application() {
    override fun onCreate() {
        super.onCreate()

        Timber.plant(Timber.DebugTree())
    }
}