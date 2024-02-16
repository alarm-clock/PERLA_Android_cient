package com.example.jmb_bms.viewModel

import androidx.lifecycle.*
import com.example.jmb_bms.model.LocationRepo
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import locus.api.android.ActionBasics
import locus.api.android.utils.LocusUtils
import java.util.Timer
import java.util.TimerTask

class LiveLocationFromLoc(private val locationRep: LocationRepo) : ViewModel() {

    private val __location = MutableLiveData<String>()     //this changes over time
    val currLocation: LiveData<String> get() = __location  // this is getter for current value

    private var collectionJob: Job? = null

    init{
        collectionJob = viewModelScope.launch {
            locationRep.getLocUpdates().collect{ update -> //collecting from locationRep flow
                __location.value = update
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        collectionJob?.cancel()
    }

    companion object {
        //static function for creating factory with additional parameter locationRepo
        //it is used as it would be used if factory was separate object
        //it is better to have it together
        fun create(locationRepo: LocationRepo): ViewModelProvider.Factory {
            return object : ViewModelProvider.Factory{
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    if (modelClass.isAssignableFrom(LiveLocationFromLoc::class.java)) {
                        return LiveLocationFromLoc(locationRepo) as T
                    }
                    throw IllegalArgumentException("Unknown ViewModel class")
                }
            }
        }
    }
}