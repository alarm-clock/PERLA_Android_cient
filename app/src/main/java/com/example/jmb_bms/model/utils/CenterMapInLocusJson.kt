package com.example.jmb_bms.model.utils

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

fun centerMapInLocusJson(lat: Double, long: Double, zoom: Int = 20) = "{ map_move_zoom: { move_to_lon: \"$long\", move_to_lat: \"$lat\", zoom: $zoom } }"
