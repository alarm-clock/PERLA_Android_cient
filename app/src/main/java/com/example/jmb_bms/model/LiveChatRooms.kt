package com.example.jmb_bms.model

import android.content.Context
import android.util.Log
import androidx.core.content.edit
import androidx.lifecycle.MutableLiveData
import com.example.jmb_bms.connectionService.in_app_communication.ChatRoomsCallBacks
import com.example.jmb_bms.model.database.chat.ChatDBHelper
import com.example.jmb_bms.model.database.chat.ChatRow
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import java.util.NoSuchElementException
import kotlin.concurrent.thread

class LiveChatRooms(
    val dbHelper: ChatDBHelper,
    val ctx: Context,
    val mainAreaLoading: MutableLiveData<Boolean>,
    val sideAreaLoading: MutableLiveData<Boolean>
) : ChatRoomsCallBacks
{

    val chatRooms = MutableStateFlow(listOf<ChatInfoAndMsgHolder>())

    val pickedRoom = MutableStateFlow<ChatInfoAndMsgHolder?>(null)

    private val shPref = ctx.getSharedPreferences("jmb_bms_Server_Info", Context.MODE_PRIVATE)

    init {
        CoroutineScope(Dispatchers.IO).launch {
            mainAreaLoading.postValue(true)
            sideAreaLoading.postValue(true)
            dbHelper.getAllChatRooms()?.let {

                val holders = it.map {row -> ChatInfoAndMsgHolder(MutableStateFlow(row),MutableStateFlow(listOf())) }
                val lastPickedRoomId = shPref.getString("Last_Chat_Room",null)
                val lastRoom: ChatInfoAndMsgHolder? = if(lastPickedRoomId != null)
                {
                    holders.find { holder -> holder.info.value.id == lastPickedRoomId }
                } else
                {
                    try {
                        editShPref(it.first().id)
                        holders.first()

                    } catch (_: NoSuchElementException)
                    {
                        null
                    }
                }
                CoroutineScope(Dispatchers.Main).launch {
                    chatRooms.value = holders
                    pickedRoom.value = lastRoom
                    mainAreaLoading.value = false
                    sideAreaLoading.value = false
                }
            }
        }
    }

    private fun editShPref(newId: String)
    {
        thread {
            shPref.edit {
                putString("Last_Chat_Room",newId)
                commit()
            }
        }
    }

    override fun manageRooms(id: String, add: Boolean) {

        val tmp = chatRooms.value.toMutableList()

        if(add)
        {

            val row = dbHelper.getChatRoom(id) ?: return
            val existing = tmp.find { it.info.value.id == id }

            if(existing != null) {
                CoroutineScope(Dispatchers.Main).launch {
                    existing.info.value = row
                }
                return
            }
            tmp.add(
                ChatInfoAndMsgHolder(
                    MutableStateFlow(row),
                    MutableStateFlow(listOf())
                )
            )
            if(pickedRoom.value == null)
            {
                pickRoom(row.id)
            }
        } else
        {
            tmp.removeIf{ it.info.value.id == id}
            if(pickedRoom.value?.info?.value?.id == id)
            {
                CoroutineScope(Dispatchers.Main).launch {
                    pickedRoom.value = null
                }
            }
        }
        CoroutineScope(Dispatchers.Main).launch {
            chatRooms.value = tmp
        }
    }

    override fun parseMessage(message: ChatMessage) {

        Log.d("LiveChatRooms","Parsing message $message")

        val holder = chatRooms.value.find{ it.info.value.id == message.chatRoomId } ?: return

        Log.d("LiveChatRooms","Found holder for this message $holder")

        val msgList = holder.messages.value.toMutableList()
        message.renderSymbol(ctx)
        msgList.addIfUnique(message)

        val msgListNoDuplicates = msgList.distinct().toMutableList()
        msgListNoDuplicates.sortBy { it.id }


        Log.d("LiveChatRooms","Added message and sorted list")

        CoroutineScope(Dispatchers.Main).launch {
            holder.messages.value = msgListNoDuplicates
        }
    }

    override fun parseMultipleMessages(messages: List<ChatMessage>, chatRoomId: String) {
        val holder = chatRooms.value.find { it.info.value.id == chatRoomId } ?: return
        val msgList = holder.messages.value.toMutableList()

        messages.forEach {
            it.renderSymbol(ctx)
            msgList.addIfUnique(it)
        }
        msgList.sortBy { it.id }

        CoroutineScope(Dispatchers.Main).launch {
            holder.messages.value = msgList
        }
    }

    fun pickRoom(chatRoomId: String)
    {
        CoroutineScope(Dispatchers.IO).launch {
            mainAreaLoading.postValue(true)

            val room = chatRooms.value.find { it.info.value.id == chatRoomId }

            if(room == null)
            {
                mainAreaLoading.postValue(false)
                CoroutineScope(Dispatchers.Main).launch {
                    pickedRoom.value = null
                }
                return@launch
            }
            editShPref(room.info.value.id)
            CoroutineScope(Dispatchers.Main).launch{
                pickedRoom.value = room
            }
        }
    }

    fun MutableList<ChatMessage>.addIfUnique(value: ChatMessage){
        if(!contains(value))
        {
            add(0,value)
        }
    }
}