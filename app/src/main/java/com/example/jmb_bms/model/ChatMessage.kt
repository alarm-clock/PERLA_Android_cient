/**
 * @file: ChatMessage.kt
 * @author: Jozef Michal Bukas <xbukas00@stud.fit.vutbr.cz,jozefmbukas@gmail.com>
 * Description: File containing ChatMessage class
 */
package com.example.jmb_bms.model

import android.content.Context
import android.net.Uri
import com.example.jmb_bms.model.database.points.MenuRow
import com.example.jmb_bms.model.icons.Symbol

/**
 * Data class that holds message data and metadata
 * @param id Message ID that also determines message order
 * @param chatRoomId ID room in which message was sent
 * @param text Message text
 * @param files List of files attached to message
 * @param points List of points attached to message
 * @param userName Username of user that sent message
 * @param userSymbol String code of users symbol
 * @param userSymbolImage Rendered symbol
 */
data class ChatMessage(
    val id: Long,
    val chatRoomId: String,
    val text: String,
    val files: List<Uri>?,
    val points: List<MenuRow>?,
    val userName: String,
    val userSymbol: String,
    var userSymbolImage: Symbol? = null
){
    /**
     * Method that renders symbol with [userSymbol] code
     * @param ctx Context for rendering
     */
    fun renderSymbol(ctx: Context)
    {
        userSymbolImage =  Symbol(userSymbol,ctx,"65")
    }

    override fun equals(other: Any?): Boolean {
        return if(other is ChatMessage){
            //messages are equal if they have same id
            id == other.id
        } else false
    }

    override fun hashCode(): Int {
        return super.hashCode()
    }
}
