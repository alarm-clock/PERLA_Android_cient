package com.example.jmb_bms.model

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.jmb_bms.connectionService.ConnectionState
import com.example.jmb_bms.connectionService.in_app_communication.ServiceStateCallback

class LiveServiceState: ServiceStateCallback {

    private val _connectionState = MutableLiveData(ConnectionState.NONE)
    val connectionState: LiveData<ConnectionState> = _connectionState

    private val _connectionErrorMsg = MutableLiveData("")
    val connectionErrorMsg: LiveData<String> = _connectionErrorMsg

    override fun onOnServiceStateChanged(newState: ConnectionState) {
        if(newState == _connectionState.value) return
        _connectionState.postValue(newState)
    }

    override fun onServiceErroStringChange(new: String) {
        _connectionErrorMsg.postValue(new)
    }
}