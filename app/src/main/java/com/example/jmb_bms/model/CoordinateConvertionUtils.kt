package com.example.jmb_bms.model

import com.google.android.gms.maps.model.LatLng
import locus.api.objects.extra.Location
import mil.nga.mgrs.MGRS

//conversion from lat long to MGRS and adding some spaces for better readability
fun wgs84toMGRS(loc: Location) = addSpacesInTheMGRScoordinate( MGRS.from(loc.longitude,loc.latitude).toString() ) ?: "Error in conversion"

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
