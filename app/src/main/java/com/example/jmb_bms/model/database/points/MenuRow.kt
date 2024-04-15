package com.example.jmb_bms.model.database.points

data class MenuRow(
    val id: Long,
    val name: String,
    val visible: Boolean,
    val symbol: String,
    val ownedByClient: Boolean,
    val online: Boolean
)
