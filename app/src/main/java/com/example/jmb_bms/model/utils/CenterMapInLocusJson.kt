/**
 * @file: CenterMapInLocusJson.kt
 * @author: Jozef Michal Bukas <xbukas00@stud.fit.vutbr.cz,jozefmbukas@gmail.com>
 * Description: File containing function that returns correct JSON that is used in Locus action
 */
package com.example.jmb_bms.model.utils

/**
 * Function that returns JSON used for Locus's map move action that sets map on given [lat]/[long] coordinates with given [zoom]
 * @param lat Latitude
 * @param long Longitude
 * @param zoom Map zoom
 * @return JSON string that can be sent with intent to Locus
 */
fun centerMapInLocusJson(lat: Double, long: Double, zoom: Int = 20) = "{ map_move_zoom: { move_to_lon: \"$long\", move_to_lat: \"$lat\", zoom: $zoom } }"
