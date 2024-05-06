/**
 * @file: ChatInfoAndMsgHolder.kt
 * @author: Jozef Michal Bukas <xbukas00@stud.fit.vutbr.cz,jozefmbukas@gmail.com>
 * Description: File containing ChatInfoAndMsgHolder class
 */
package com.example.jmb_bms.model

import com.example.jmb_bms.model.database.chat.ChatRow
import kotlinx.coroutines.flow.MutableStateFlow

/**
 * Data class that holds chat room info and all messages in it
 * @param info Live [ChatRow] object with data about chat room
 * @param messages Live [List] of [ChatMessage]s that were sent in chat room
 */
data class ChatInfoAndMsgHolder(
    val info: MutableStateFlow<ChatRow>,
    val messages: MutableStateFlow<List<ChatMessage>>
)
