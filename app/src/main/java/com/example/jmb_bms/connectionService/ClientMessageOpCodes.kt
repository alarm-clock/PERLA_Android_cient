package com.example.jmb_bms.connectionService

enum class ClientMessageOpCodes(identification: String) {
    HELLO("HELLO_THERE"),
    SEND_USER_DATA("1"),
    SEND_LOCATION("3"),
    STOP_SENDING_LOCATION("3"),
    BYE("0")

    //update user data
}