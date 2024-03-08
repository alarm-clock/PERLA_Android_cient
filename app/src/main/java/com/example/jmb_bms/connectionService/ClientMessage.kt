package com.example.jmb_bms.connectionService

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

fun userInfo(profile: UserProfile): String
{
    return buildJsonObject {
        put("opCode",1)
        put("userName",profile.userName)
        put("symbolCode",profile.symbolCode)
        put("_id",profile.serverId)
    }.toString()
}

fun locationUpdate(update: String): String
{
    return buildJsonObject {
        put("opCode",3)
        put("lat", update.substring(0,update.indexOfFirst { it == '-' }).toDouble())
        put("long", update.substring(update.indexOfFirst { it == '-' } + 1, update.length).toDouble())
    }.toString()
}

fun stopUpdating(): String
{
    return  buildJsonObject {
        put("opCode",3)
        put("lat","stop")
        put("long","stop")
    }.toString()
}
fun bye(reason: String): String
{
    return buildJsonObject {
        put("opCode",0)
        put("reason",reason)
    }.toString()
}
fun helloThere(): String
{
    val jsonObj = buildJsonObject{
        put("opCode",-1)
    }
    return jsonObj.toString()
}

fun parseServerJson(json: String): Map<String, Any?>
{
    val gson = Gson()
    val type = object : TypeToken<Map<String, Any?>>() {}.type
    val map: Map<String, Any?> = gson.fromJson(json,type)
    return map
}

fun getOpcode(map: Map<String, Any?>): Double? {
    return map["opCode"] as? Double
}