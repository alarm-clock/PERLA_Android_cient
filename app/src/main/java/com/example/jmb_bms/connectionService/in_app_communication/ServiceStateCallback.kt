/**
 * @file:  ServiceStateCallback.kt
 * @author: Jozef Michal Bukas <xbukas00@stud.fit.vutbr.cz,jozefmbukas@gmail.com>
 * Description: File containing ServiceStateCallback interface
 */
package com.example.jmb_bms.connectionService.in_app_communication

import com.example.jmb_bms.connectionService.ConnectionState

/**
 * Interface that ahs callback methods implemented by observers that want service state updates
 */
interface ServiceStateCallback {

    /**
     * Callback with new [ConnectionState]
     * @param newState New connection state
     */
    fun onOnServiceStateChanged(newState: ConnectionState)

    /**
     * Callback with error string if some error occurred
     * @param new New error string
     */
    fun onServiceErroStringChange(new: String)
}