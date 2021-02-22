package com.amati.deus.ui.home

import android.os.SystemClock
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import timber.log.Timber

class MainViewModel : ViewModel() {

    private val interval: Long = 1000
    private val scope  = MainScope()
    private var base = SystemClock.elapsedRealtime()
    private var job: Job? = null
    private var elapsedTime: Long = 0

    private val elapsedTimeMutableLiveData = MutableLiveData<Long>()
    private var countUpTimer: CountUpTimer = CountUpTimer()

    init {

    }

    fun getElapsedTime(): MutableLiveData<Long> {
        return elapsedTimeMutableLiveData
    }

    fun startTimerAndRecord() {
        startTimer()
    }

    private fun startTimer(){
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
        elapsedTimeMutableLiveData.postValue(elapsedTime)
    }

    private fun returnElapsedTime():Long{
        Timber.e(elapsedTime.toString())
        return elapsedTime
    }

    fun stopTimer(){
        job?.cancel()
        job = null
    }
}