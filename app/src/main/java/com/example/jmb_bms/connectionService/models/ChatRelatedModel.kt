package com.example.jmb_bms.connectionService.models

import android.net.Uri
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.jmb_bms.connectionService.ClientMessage
import com.example.jmb_bms.connectionService.ConnectionService
import com.example.jmb_bms.connectionService.FileSharingRequests
import com.example.jmb_bms.connectionService.in_app_communication.InnerCommunicationCentral
import com.example.jmb_bms.connectionService.parseServerJson
import com.example.jmb_bms.model.ChatMessage
import com.example.jmb_bms.model.database.chat.ChatDBHelper
import com.example.jmb_bms.model.database.chat.ChatRow
import com.example.jmb_bms.model.database.points.MenuRow
import com.example.jmb_bms.model.utils.sendNotification
import io.ktor.websocket.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class ChatRelatedModel(val service: ConnectionService, val communicationCentral: InnerCommunicationCentral) {

    val dbHelper = ChatDBHelper(service,null)

    fun parseChatCreation(params: Map<String, Any?>)
    {
        CoroutineScope(Dispatchers.IO).launch {
            val id = params["_id"] as? String ?: return@launch
            val name = params["name"] as? String ?: return@launch
            val ownerId = params["ownerId"] as? String ?: return@launch
            val members = params["members"] as? List<String>

            Log.d("ChatRelatedModel","parsed message and creating chat room $name")

            val newRoom = ChatRow(id,name,ownerId,members ?: listOf())

            dbHelper.getChatRoom(id)?.let {
                Log.d("ChatRelatedModel","chat room with id $id already exists so I'm updating it")
                dbHelper.updateChatRoom(newRoom)
            } ?: dbHelper.addChatRoom(newRoom)

            communicationCentral.sendChatRoomsUpdate(id,true)
        }

    }

    fun parseChatDeletion(params: Map<String, Any?>)
    {
        val id = params["_id"] as? String ?: return

        Log.d("ChatRelatedModel","Deleting chat with id $id")
        dbHelper.removeChatRoom(id)

        //TODO delete files

        communicationCentral.sendChatRoomsUpdate(id,false)
    }

    private fun createMsgFromParams(params: Map<String, Any?>): ChatMessage? {
        val id = params["_id"] as? Double ?: return null
        val chatId = params["chatId"] as? String ?: return null
        val ownerName = params["userName"] as? String
        val ownerSymbol = params["userSymbol"] as? String
        val text = params["text"] as? String
        val files = params["files"] as? List<String>

        val uriList = files?.map { Uri.parse(it) }

        val points = params["points"] as? List<String>

        val pointsRow = points?.map {
            service.pointModel.dbHelper.getMenuRowByServerId(it) ?: MenuRow(-2, "Not Existing", false, "", false, false)
        }

        return ChatMessage(
            id.toLong(),
            chatId,
            text ?: "",
            uriList ?: listOf(),
            pointsRow ?: listOf(),
            ownerName ?: "",
            ownerSymbol ?: ""
        )
    }

    fun parseMessage(params: Map<String, Any?>)
    {
        CoroutineScope(Dispatchers.IO).launch {
            Log.d("ChatRelatedModel", "Got new message")

            val message = createMsgFromParams(params) ?: return@launch
            Log.d("ChatRelatedModel","Parsing message")
            communicationCentral.sendChatMessage(message)
        }
    }

    private fun parseMultipleMessages(messages: List<String>): List<ChatMessage>
    {
        val res = mutableListOf<ChatMessage>()

        messages.forEach {
            val params = parseServerJson(it)
            val msg = createMsgFromParams(params)

            if(msg != null)
            {
                res.add(msg)
            }
        }
        return res
    }

    fun parseMultipleMessages(params: Map<String, Any?>)
    {
        CoroutineScope(Dispatchers.IO).launch {
            val messages = params["messages"] as? List<String> ?: return@launch

            val msg = parseMultipleMessages(messages)

            communicationCentral.sendMultipleChatMessages(msg)
        }
    }


    suspend fun sendMessage(message: ChatMessage)
    {
        val transactionId = "${service.serviceModel.profile.serverId}@${
            SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(
                Date()
            )}@m"

        val requestMaker = FileSharingRequests(service,communicationCentral)

        if(message.files != null) {
            val res = requestMaker.sendMultipleFiles(transactionId, message.files) {

                sendNotification(
                    service,
                    "Failed to send message",
                    "Failed to upload file... try again",
                    NotificationCompat.PRIORITY_HIGH,
                    transactionId
                )
                service.session.send(Frame.Text(ClientMessage.failedTransactionAck(transactionId)))
            }

            if(!res) return
        }
        service.sendMessage(ClientMessage.sendMessage(message,transactionId))
    }

    fun fetchNewestMessages()
    {
        CoroutineScope(Dispatchers.IO).launch {
            val rooms = dbHelper.getAllChatRooms()
            rooms?.forEach {
                Log.d("ChatRelatedModel","Fetching newest messages for room ${it.name}")
                service.sendMessage(ClientMessage.fetchMessages(-1,it.id))
            }
        }
    }
}