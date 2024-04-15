package com.example.jmb_bms.connectionService.in_app_communication

import com.example.jmb_bms.model.ChatMessage

interface ChatRoomsCallBacks {
    fun manageRooms(id: String, add: Boolean)

    fun parseMessage(message: ChatMessage)

    fun parseMultipleMessages(messages: List<ChatMessage>, chatRoomId: String)
}