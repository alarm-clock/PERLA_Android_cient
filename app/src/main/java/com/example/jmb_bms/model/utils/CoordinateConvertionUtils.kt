/**
 * @file: CoordinateConvertionUtils.kt
 * @author: Jozef Michal Bukas <xbukas00@stud.fit.vutbr.cz,jozefmbukas@gmail.com>
 * Description: File containing functions for converting lat/long coordinates to MGRS
 */
package com.example.jmb_bms.model.utils

import locus.api.objects.extra.Location
import mil.nga.mgrs.MGRS

/**
 * Function for converting from lat/long to MGRS
 * @param loc Location object with lat/long coordinates
 * @return String with MGRS coordinates
 */
fun wgs84toMGRS(loc: Location) = addSpacesInTheMGRScoordinate( MGRS.from(loc.longitude,loc.latitude).toString() ) ?: "Error in conversion"

/**
 * Function for converting from lat/long to MGRS
 * @param loc Location object with lat/long coordinates
 * @return String with MGRS coordinates
 */
fun wgs84toMGRS(loc: android.location.Location) = addSpacesInTheMGRScoordinate( MGRS.from(loc.longitude,loc.latitude).toString() ) ?: "Error in conversion"

/**
 * Function that adds spaces on correct places in MGRS string for better readability
 * @param coordinate String with MGRS coordinate without spaces
 * @return String with MGRS coordinate that has spaces for better readability or null string doesn't have correct length
 */
fun addSpacesInTheMGRScoordinate(coordinate: String): String?
{
    var _coordinate = coordinate
    val indexes : Array<Int> = when(coordinate.length) {
        15 -> arrayOf(2,6,12)
        13 -> arrayOf(2,6,11)
        11 -> arrayOf(2,6,10)
        9 -> arrayOf(2,6,9)
        else -> return null
    }
    for(index in indexes)
    {
       _coordinate = StringBuilder(_coordinate).insert(index,' ').toString()
    }
    return _coordinate
}
