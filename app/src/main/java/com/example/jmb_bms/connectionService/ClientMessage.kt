/**
 * @file: ClientMessage.kt
 * @author: Jozef Michal Bukas <xbukas00@stud.fit.vutbr.cz,jozefmbukas@gmail.com>
 * Description: File containing Client message object and methods for parsing JSON
 */
package com.example.jmb_bms.connectionService

import android.content.Context
import com.example.jmb_bms.connectionService.models.UserProfile
import com.example.jmb_bms.model.ChatMessage
import com.example.jmb_bms.model.database.points.PointRow
import com.example.jmb_bms.model.utils.getOriginalFileName
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.*

/**
 * Object that implements methods that create JSON messages sent to server
 */
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

        fun teamLocationUpdate(teamId: String, update: String): String
        {
            return buildJsonObject {
                put(OPCODE, 29)
                put("_id", teamId)
                put("lat", update.substring(0,update.indexOfFirst { it == '-' }).toDouble())
                put("long", update.substring(update.indexOfFirst { it == '-' } + 1, update.length).toDouble())
            }.toString()
        }

        fun teamLocationUpdatingStop(teamId: String): String
        {
            return buildJsonObject {
                put(OPCODE, 29)
                put("_id",teamId)
            }.toString()
        }

        fun failedTransactionAck(id: String): String
        {
            return buildJsonObject {
                put(OPCODE,49)
                put("transactionId",id)
            }.toString()
        }

        @OptIn(ExperimentalSerializationApi::class)
        fun createPoint(point: PointRow, context: Context): String
        {
            return buildJsonObject {
                put(OPCODE,40)
                put("serverId",point.serverId)
                put("name",point.name)
                put("descr",point.descr)
                put("symbol",point.symbol)
                put("menuStr",point.menuString)
                putJsonArray("files"){
                    this.addAll(point.uris?.map {
                         it.getOriginalFileName(context)
                    }?.toList() ?: listOf()
                    )
                }
                put("owner",point.ownerId)
                put("lat",point.location.latitude)
                put("long",point.location.longitude)

            }.toString()
        }

        fun deletePoint(serverId: String): String
        {
            return buildJsonObject {
                put(OPCODE,42)
                put("serverId",serverId)
            }.toString()
        }

        @OptIn(ExperimentalSerializationApi::class)
        fun syncRequest(ids: List<String>): String
        {
            return buildJsonObject {
                put(OPCODE,44)
                putJsonArray("ids"){
                    this.addAll(ids)
                }
            }.toString()
        }

        fun fetchMessages(cap: Long, chatId: String): String
        {
            return buildJsonObject {
                put(OPCODE,65)
                put("_id",chatId)
                put("cap",cap)
            }.toString()
        }

        @OptIn(ExperimentalSerializationApi::class)
        fun sendMessage(message: ChatMessage, transactionId: String): String
        {
            return buildJsonObject {
                put(OPCODE, 64)
                put("transactionId", transactionId)
                put("_id",message.chatRoomId)
                put("text", message.text)
                if (message.files != null) {
                    putJsonArray("files") {
                        this.addAll(message.files.map { it.toString() })
                    }
                }
                if(message.points != null) {
                    putJsonArray("points") {
                        this.addAll(message.points.map { it.id.toString() })
                    }
                }

            }.toString()
        }

        @OptIn(ExperimentalSerializationApi::class)
        fun createChatRoom(name: String, members: List<String>): String
        {
            return buildJsonObject {
                put(OPCODE,60)
                put("name",name)
                putJsonArray("memberIds"){
                    this.addAll(members)
                }
            }.toString()
        }

        fun deleteChatRoom(id: String): String
        {
            return buildJsonObject {
                put(OPCODE,61)
                put("_id",id)
            }.toString()
        }

        @OptIn(ExperimentalSerializationApi::class)
        fun manageChatUsers(id: String, users: List<String>, add: Boolean): String
        {
            return buildJsonObject {
                put(OPCODE,62)
                put("_id",id)
                put("add",add)
                putJsonArray("userIds"){
                    addAll(users)
                }
            }.toString()
        }

        /*fun updatePoint(point: PointRow, context: Context): String
        {
            return buildJsonObject {
                put(OPCODE,43)
                put("serverId",point.serverId)
                put("name",point.name)
                put("descr",point.descr)
                put("symbol",point.symbol)
                put("menuStr",point.menuString)

            }.toString()
        }*/
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