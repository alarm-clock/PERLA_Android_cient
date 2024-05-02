package com.example.jmb_bms.viewModel.chat

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.jmb_bms.model.LiveChatUsersMng
import com.example.jmb_bms.model.LiveServiceState
import com.example.jmb_bms.model.LiveUsers
import com.example.jmb_bms.model.database.chat.ChatDBHelper
import com.example.jmb_bms.viewModel.ServiceBinder

class ChatRoomUsersVM(ctx: Context, roomId: String, adding: Boolean): ViewModel() {

    private val dbHelper = ChatDBHelper(ctx, null)
    val liveUsers = LiveUsers(ctx)
    val liveServiceState = LiveServiceState()
    val liveChatUsersMng = LiveChatUsersMng(roomId, dbHelper, liveUsers, ctx, adding)

    val serviceBinder = ServiceBinder(ctx, listOf(liveServiceState,liveUsers,liveChatUsersMng))

    val userId = serviceBinder.service?.getUserId()

    val pickedUsers = mutableListOf<String>()

    fun sendUpdate()
    {

    }

    override fun onCleared() {
        super.onCleared()
        liveUsers.onDestroy()
        dbHelper.close()
        serviceBinder.unbind()
    }

    companion object{
        fun create(context: Context, roomId: String, adding: Boolean): ViewModelProvider.Factory{

            return object : ViewModelProvider.Factory{
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    if( modelClass.isAssignableFrom(ChatRoomUsersVM::class.java) )
                    {
                        return ChatRoomUsersVM( context, roomId, adding) as T
                    }
                    throw IllegalArgumentException("Unknown VM class")
                }
            }
        }
    }
}