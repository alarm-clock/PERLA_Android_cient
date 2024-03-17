package com.example.jmb_bms.connectionService.in_app_communication

import com.example.jmb_bms.connectionService.ConnectionState

interface ServiceStateCallback {
    fun onOnServiceStateChanged(newState: ConnectionState)

    fun onServiceErroStringChange(new: String)
}