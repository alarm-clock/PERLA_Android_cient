package com.example.jmb_bms.connectionService

interface ServiceStateCallback {
    fun onOnServiceStateChanged(newState: ConnectionState)

    fun onServiceErroStringChange(new: String)
}