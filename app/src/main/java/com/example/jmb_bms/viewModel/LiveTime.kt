package com.example.jmb_bms.viewModel

import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import java.text.SimpleDateFormat
import java.util.*

class LiveTime : ViewModel()  { //ViewModel() {

    private val __curentTime = MutableLiveData<String>()
    val currentTime: LiveData<String> get() = __curentTime

    private var timer: Timer? = null

    init
    {
        timer?.cancel()
        timer = Timer()
        storeCurrentTime()

        timer?.scheduleAtFixedRate( object: TimerTask()
        {
            override fun run() {
                storeCurrentTime()
            }
        }, 0, 1000)
    }

    private fun storeCurrentTime()
    {
        __curentTime.postValue(SimpleDateFormat("HH:mm:ssXXX", Locale.getDefault()).format(Date()))
    }

    fun stopTimer()
    {
        timer?.cancel()
        timer = null
    }

    override fun onCleared() {
        super.onCleared()
        timer?.cancel()
    }
}