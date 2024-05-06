/**
 * @file: LiveTime.kt
 * @author: Jozef Michal Bukas <xbukas00@stud.fit.vutbr.cz,jozefmbukas@gmail.com>
 * Description: File containing LiveTime class
 */
package com.example.jmb_bms.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import java.text.SimpleDateFormat
import java.util.*

/**
 * ViewModel which holds and offers live time updated every second
 *
 * @constructor Stores current time and then starts timer which runs [storeCurrentTime] method every second
 */
class LiveTime : ViewModel()  {

    private val __currentTime = MutableLiveData<String>()

    /**
     * Live time updated every second
     */
    val currentTime: LiveData<String> get() = __currentTime

    private var timer: Timer? = null

    private val second = 1000L

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
        }, 0, second)
    }

    /**
     * Method that posts current time into [__currentTime] in `HH:mm:ssXXX` format
     */
    private fun storeCurrentTime()
    {
        __currentTime.postValue(SimpleDateFormat("HH:mm:ssXXX", Locale.getDefault()).format(Date()))
    }

    fun stopTimer()
    {
        timer?.cancel()
        timer = null
    }

    override fun onCleared() {
        super.onCleared()
        //stop timer so that it won't run into eternity
        timer?.cancel()
    }
}