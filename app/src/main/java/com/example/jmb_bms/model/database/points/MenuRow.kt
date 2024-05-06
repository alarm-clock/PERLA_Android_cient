/**
 * @file: MenuRow.kt
 * @author: Jozef Michal Bukas <xbukas00@stud.fit.vutbr.cz,jozefmbukas@gmail.com>
 * Description: File containing MenuRow class
 */
package com.example.jmb_bms.model.database.points

/**
 * Data class with points information that is shown in menu
 * @param id Points id in clients database, unique only for current client
 * @param name Points name
 * @param visible Flag indicating if point is visible on map
 * @param symbol Symbol code in 2525
 * @param ownedByClient Flag that indicates that user can update given point
 * @param online Flag indicating if point is online point
 */
data class MenuRow(
    val id: Long,
    val name: String,
    val visible: Boolean,
    val symbol: String,
    val ownedByClient: Boolean,
    val online: Boolean
)
