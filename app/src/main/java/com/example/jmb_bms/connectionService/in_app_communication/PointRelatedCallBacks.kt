/**
 * @file:  PointRelatedCallBacks.kt
 * @author: Jozef Michal Bukas <xbukas00@stud.fit.vutbr.cz,jozefmbukas@gmail.com>
 * Description: File containing PointRelatedCallBacks interface
 */
package com.example.jmb_bms.connectionService.in_app_communication

/**
 * Interface with callback methods that are implemented by observers that want point updates
 */
interface PointRelatedCallBacks {

    /**
     * Callback that notifies that point with given [id] was either updated or created
     * @param id ID of point that has changed or updated
     */
    fun parsedPoint(id: Long)

    /**
     * Callback that notifies observer about deletion of point identified by [id]
     * @param id ID of deleted point
     */
    fun deletedPoint(id: Long)
}