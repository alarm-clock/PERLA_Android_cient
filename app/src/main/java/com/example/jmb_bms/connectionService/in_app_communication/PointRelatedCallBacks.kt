package com.example.jmb_bms.connectionService.in_app_communication

interface PointRelatedCallBacks {

    fun parsedPoint(id: Long)

    fun deletedPoint(id: Long)
}