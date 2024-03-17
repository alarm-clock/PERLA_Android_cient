package com.example.jmb_bms.connectionService

enum class ConnectionState() {
    NOT_CONNECTED,
    NEGOTIATING,
    CONNECTED,
    RECONNECTING,
    ERROR,
    NONE
}