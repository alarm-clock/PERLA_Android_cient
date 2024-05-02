package com.example.jmb_bms.viewModel.chat

import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.jmb_bms.model.ChatMessage
import com.example.jmb_bms.model.LiveChatRooms
import com.example.jmb_bms.model.LiveServiceState
import com.example.jmb_bms.model.database.chat.ChatDBHelper
import com.example.jmb_bms.model.database.points.PointDBHelper
import com.example.jmb_bms.viewModel.ServiceBinder
import com.example.jmb_bms.viewModel.point.AllPointsVM
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AllChatsVM(appCtx: Context) : ViewModel() {

    val mainAreaLoading = MutableLiveData(false)
    val sideAreaLoading = MutableLiveData(false)

    private val dbHelper = ChatDBHelper(appCtx,null)
    val liveServiceState = LiveServiceState()
    val liveChatRooms = LiveChatRooms(dbHelper,appCtx,mainAreaLoading,sideAreaLoading)
    private val serviceBinder = ServiceBinder(appCtx, listOf(liveServiceState, liveChatRooms))

    private var secondTime = false
    fun sendMessage(message: String)
    {
        viewModelScope.launch(Dispatchers.IO)
        {
            val chatRoomId = liveChatRooms.pickedRoom.value?.info?.value?.id ?: return@launch
            val msg = ChatMessage(
                69,chatRoomId,message,null,null,"",""
            )
            serviceBinder.service?.sendChatMessage(msg)
        }
    }

    fun fetchMessages()
    {
        if(secondTime)
        {
            viewModelScope.launch(Dispatchers.IO){
                val room = liveChatRooms.pickedRoom.value ?: return@launch
                if(room.messages.value.isEmpty()) return@launch
                serviceBinder.service?.sendFetchMessages(room.messages.value.first().id,room.info.value.id)
            }
        } else secondTime = true
    }

    fun userIsOwner(): Boolean
    {
        val id = serviceBinder.service?.getUserId()
        return id == liveChatRooms.pickedRoom.value?.info?.value?.owner
    }

    fun leaveRoom()
    {
        viewModelScope.launch(Dispatchers.IO) {
            val room = liveChatRooms.pickedRoom.value ?: return@launch
            if(room.info.value.name != "General")
            {
                //serviceBinder.service?.sendLeaveRoom(room.info.value.id)
            }
        }
    }

    fun deleteRoom()
    {
        viewModelScope.launch(Dispatchers.IO) {
            val room = liveChatRooms.pickedRoom.value ?: return@launch
            if(room.info.value.owner == serviceBinder.service?.getUserId())
            {
                serviceBinder.service?.deleteChatRoom(room.info.value.id)
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        serviceBinder.unbind()
        dbHelper.close()
    }

    companion object{
        fun create(context: Context): ViewModelProvider.Factory{

            return object : ViewModelProvider.Factory{
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    if( modelClass.isAssignableFrom(AllChatsVM::class.java) )
                    {
                        return AllChatsVM( context) as T
                    }
                    throw IllegalArgumentException("Unknown VM class")
                }
            }
        }
    }
}