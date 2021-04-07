package com.amati.deus.services

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import timber.log.Timber

class StartReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED && getServiceState(context) == ServiceState.STARTED) {
            Intent(context, WatchingService::class.java).also {
                it.action = ServiceActions.START.name
                Timber.d("Starting the service from a broadcastReceiver")
                context.startForegroundService(it)
                return
            }
        }
    }

}