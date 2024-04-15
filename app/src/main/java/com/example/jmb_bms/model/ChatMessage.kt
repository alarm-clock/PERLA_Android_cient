package com.example.jmb_bms.model

import android.content.Context
import android.net.Uri
import com.example.jmb_bms.model.database.points.MenuRow
import com.example.jmb_bms.model.icons.Symbol

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

    fun renderSymbol(ctx: Context)
    {
        userSymbolImage =  Symbol(userSymbol,ctx,"65")
    }

    override fun equals(other: Any?): Boolean {
        return if(other is ChatMessage){
            id == other.id
        } else false
    }

    override fun hashCode(): Int {
        return super.hashCode()
    }
}
