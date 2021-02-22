package com.amati.deus.ui.home

import android.os.SystemClock
import kotlinx.coroutines.*
import timber.log.Timber

class CountUpTimer {

    private val interval: Long = 1000
    private val scope  = MainScope()
    private var base = SystemClock.elapsedRealtime()
    private var job: Job? = null
    public var elapsedTime: Long = 0

    fun startTimer(){
       base = SystemClock.elapsedRealtime()
        job = scope.launch {
            while (true){
                updateElapsedTime()
                delay(interval)
            }
        }
    }

    private fun updateElapsedTime(){
        elapsedTime += interval
    }

    fun returnElapsedTime():Long{
        Timber.e(elapsedTime.toString())
        return elapsedTime
    }

    fun stopTimer(){
        job?.cancel()
        job = null
    }
}