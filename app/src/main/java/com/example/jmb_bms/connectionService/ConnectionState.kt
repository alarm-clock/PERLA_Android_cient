/**
 * @file: ConnectionState.kt
 * @author: Jozef Michal Bukas <xbukas00@stud.fit.vutbr.cz,jozefmbukas@gmail.com>
 * Description: File containing ConnectionState class
 */
package com.example.jmb_bms.connectionService

/**
 * Enum class with all connection states
 */
enum class ConnectionState {
    NOT_CONNECTED,
    NEGOTIATING,
    CONNECTED,
    RECONNECTING,
    ERROR,
    NONE
}