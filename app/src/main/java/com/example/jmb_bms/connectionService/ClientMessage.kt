package com.example.jmb_bms.connectionService

import com.example.jmb_bms.connectionService.models.UserProfile
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put


class ClientMessage {

    companion object{

        val OPCODE = "opCode"

        fun userInfo(profile: UserProfile): String
        {
            return buildJsonObject {
                put(OPCODE,1)
                put("userName",profile.userName)
                put("symbolCode",profile.symbolCode)
                put("_id",profile.serverId)
            }.toString()
        }

        fun locationUpdate(update: String): String
        {
            return buildJsonObject {
                put(OPCODE,3)
                put("lat", update.substring(0,update.indexOfFirst { it == '-' }).toDouble())
                put("long", update.substring(update.indexOfFirst { it == '-' } + 1, update.length).toDouble())
            }.toString()
        }

        fun stopUpdating(): String
        {
            return  buildJsonObject {
                put(OPCODE,3)
                put("lat","stop")
                put("long","stop")
            }.toString()
        }
        fun bye(reason: String): String
        {
            return buildJsonObject {
                put(OPCODE,0)
                put("reason",reason)
            }.toString()
        }
        fun helloThere(): String
        {
            val jsonObj = buildJsonObject{
                put(OPCODE,-1)
            }
            return jsonObj.toString()
        }

        fun createTeam(teamName: String, teamIcon: String, topTeamId: String): String
        {
            return buildJsonObject {
                put(OPCODE,21)
                put("teamName",teamName)
                put("teamIcon", teamIcon)
                put("_id",topTeamId)
            }.toString()
        }

        fun deleteTeam(teamId: String): String
        {
            return buildJsonObject {
                put(OPCODE,22)
                put("_id",teamId)
            }.toString()
        }

        fun addOrDelUser(teamId: String, userId: String, add: Boolean): String
        {
            return buildJsonObject {
                put(OPCODE,23)
                put("_id",teamId)
                put("memberId",userId)
                put("adding",add)
            }.toString()
        }

        fun changeTeamLeader(teamId: String, newLeaderId: String): String
        {
            return buildJsonObject {
                put(OPCODE,24)
                put("_id",teamId)
                put("newLeaderId",newLeaderId)
            }.toString()
        }

        fun updateTeam(teamId: String, newName: String, newIcon: String): String
        {
            return buildJsonObject {
                put(OPCODE,25)
                put("_id",teamId)
                put("newName",newName)
                put("newIcon",newIcon)
            }.toString()
        }
        fun teamLocShSate(teamId: String,on: Boolean): String
        {
            return buildJsonObject {
                put(OPCODE,26)
                put("_id",teamId)
                put("on",on)
            }.toString()
        }

        fun userLocToggle(teamId: String, userId: String): String
        {
             return buildJsonObject {
                 put(OPCODE,27)
                 put("_id",teamId)
                 put("userId",userId)
             }.toString()
        }
    }
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