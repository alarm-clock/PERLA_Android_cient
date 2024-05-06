/**
 * @file: ChatRow.kt
 * @author: Jozef Michal Bukas <xbukas00@stud.fit.vutbr.cz,jozefmbukas@gmail.com>
 * Description: File containing ChatRow class
 */
package com.example.jmb_bms.model.database.chat

/**
 * Data class representing row in chat database.
 * @param id Chats ID
 * @param name Chats name
 * @param owner Server ID of user that owns chat
 * @param members [List] of server IDs of all users that are members of chat room
 */
data class ChatRow(
    val id: String,
    val name: String,
    val owner: String,
    val members: List<String>
)
