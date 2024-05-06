/**
 * @file: ChatRoomsCallBacks.kt
 * @author: Jozef Michal Bukas <xbukas00@stud.fit.vutbr.cz,jozefmbukas@gmail.com>
 * Description: File containing ChatRoomsCallBacks interface
 */
package com.example.jmb_bms.connectionService.in_app_communication

import com.example.jmb_bms.model.ChatMessage

/**
 * Interface with callbacks for observers that want to receive chat updates and messages
 */
interface ChatRoomsCallBacks {
    /**
     * Method that adds or removes Chat room identified by [id] based on [add] flag
     * @param id Chat room id that is added or removed
     * @param add Flag indicating if Chat room identified by [id] is added or removed
     */
    fun manageRooms(id: String, add: Boolean)

    /**
     * Method that parses [ChatMessage]
     * @param message [ChatMessage] received from server
     */
    fun parseMessage(message: ChatMessage)

    /**
     * Method that parses multiple [ChatMessage]s
     * @param messages [List] of [ChatMessage]s
     * @param chatRoomId ID of room in which all [messages] are
     */
    fun parseMultipleMessages(messages: List<ChatMessage>, chatRoomId: String)
}