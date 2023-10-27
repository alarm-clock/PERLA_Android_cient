package com.example.jmb_bms

enum class ConnectionErr(val type: String) {

    NO_ERR("No error"),
    INVALID_HOST("Could not resolve provided host"),
    INVALID_PORT("Provided port is invalid"),
    COULD_NOT_CONNECT("Could not connect to server with provided information"),
    CONNECTION_RESET_BY_SERVER("Connection was reseted by server"),
    DISCONNECTED("User was disconnected by server"),
    UNKNOWN_SERVER_RESPONSE("Server sent unknown response")
}