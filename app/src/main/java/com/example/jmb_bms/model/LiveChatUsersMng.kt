package com.example.jmb_bms.model

import android.content.Context
import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.example.jmb_bms.connectionService.in_app_communication.ChatRoomsCallBacks
import com.example.jmb_bms.connectionService.models.UserProfile
import com.example.jmb_bms.model.database.chat.ChatDBHelper
import com.example.jmb_bms.model.icons.Symbol
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

class LiveChatUsersMng(val roomId: String, val dbHelper: ChatDBHelper,val liveUsers: LiveUsers, val ctx: Context, private val adding: Boolean) : ChatRoomsCallBacks {

    val pickedRoom: MutableStateFlow<ChatInfoAndMsgHolder?> = MutableStateFlow(null)

    val chatUsersProfiles = MutableLiveData<MutableList<MutableLiveData<UserProfile>>>(mutableListOf())
    val otherUsers = MutableLiveData<MutableList<MutableLiveData<UserProfile>>>(mutableListOf())

    init {
        CoroutineScope(Dispatchers.IO).launch {
            dbHelper.getChatRoom(roomId)?.let {
                pickedRoom.value = ChatInfoAndMsgHolder(MutableStateFlow(it), MutableStateFlow(listOf()))
                setUserProfiles(it.members)
            }
        }
    }

    private fun setUserProfiles(members: List<String>)
    {
        if(adding)
        {
            val userProfiles = liveUsers.connectedUsers.value.filterNot { profile -> profile.value?.serverId in members }
            userProfiles.forEach {
                it.value!!.symbol = Symbol(it.value!!.symbolCode,ctx)
            }
            otherUsers.value?.addAll(userProfiles)
        } else
        {
            val userProfiles = liveUsers.connectedUsers.value.filter { profile -> profile.value?.serverId in members }
            userProfiles.forEach {
                it.value!!.symbol = Symbol(it.value!!.symbolCode,ctx)
            }
            chatUsersProfiles.value?.addAll(userProfiles)
        }

    }
    override fun manageRooms(id: String, add: Boolean) {
        if(id != pickedRoom.value?.info?.value?.id) return

        if(add)
        {
            val room = dbHelper.getChatRoom(id)
            if(room == null)
            {
                Log.d("LivePickedRoomDetail","In add branch but chat room does not exists in database")
                CoroutineScope(Dispatchers.Main).launch {
                    pickedRoom.value = null
                }
                return
            }
            CoroutineScope(Dispatchers.Main).launch {
                pickedRoom.value?.info?.value = room
            }
            setUserProfiles(room.members)

        } else
        {
            chatUsersProfiles.value?.clear()

            CoroutineScope(Dispatchers.Main).launch {
                pickedRoom.value = null
            }
        }
    }

    override fun parseMessage(message: ChatMessage) {}
    override fun parseMultipleMessages(messages: List<ChatMessage>, chatRoomId: String) {}
}