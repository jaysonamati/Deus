package com.amati.deus.ui.home

import androidx.lifecycle.*
import com.amati.deus.database.SensorRepository
import com.amati.deus.models.DeviceSensor
import kotlinx.coroutines.launch

class SensorViewModel(private val repository: SensorRepository) : ViewModel() {

    // Using LiveData and caching what allWords returns has several benefits:
    // - We can put an observer on the data (instead of polling for changes) and only update the
    //   the UI when the data actually changes.
    // - Repository is completely separated from the UI through the ViewModel.
    val allSensors: LiveData<List<DeviceSensor>> = repository.allSensors.asLiveData()

    /**
     * Launching a new coroutine to insert the data in a non-blocking way
     */
    fun insert(deviceSensor: DeviceSensor) = viewModelScope.launch {
        repository.insert(deviceSensor)
    }
}

class SensorViewModelFactory(private val repository: SensorRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SensorViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return SensorViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}