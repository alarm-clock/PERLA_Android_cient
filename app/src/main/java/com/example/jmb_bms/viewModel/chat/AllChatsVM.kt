/**
 * @file: AllChatsVM.kt
 * @author: Jozef Michal Bukas <xbukas00@stud.fit.vutbr.cz,jozefmbukas@gmail.com>
 * Description: File containing AllChatsVM class
 */
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

/**
 * ViewModel for chats screen that holds all data and implements all necessary methods required by chat feature
 * @param appCtx Application context for symbol rendering and binding to service.
 */
class AllChatsVM(appCtx: Context) : ViewModel() {

    val mainAreaLoading = MutableLiveData(false)
    val sideAreaLoading = MutableLiveData(false)

    private val dbHelper = ChatDBHelper(appCtx,null)
    val liveServiceState = LiveServiceState()
    val liveChatRooms = LiveChatRooms(dbHelper,appCtx,mainAreaLoading,sideAreaLoading)
    private val serviceBinder = ServiceBinder(appCtx, listOf(liveServiceState, liveChatRooms))

    private var secondTime = false

    /**
     * Method that creates [ChatMessage] instance and sends it to server
     * @param message Message that will be sent
     */
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

    /**
     * Method that sends fetch message to get older messages from server
     */
    fun fetchMessages()
    {
        //this is here because this would be sent first time screen is drawn, but it is not required because service
        //is already fetching messages
        // so first time it is ignored
        if(secondTime)
        {
            viewModelScope.launch(Dispatchers.IO){
                val room = liveChatRooms.pickedRoom.value ?: return@launch
                if(room.messages.value.isEmpty()) return@launch
                serviceBinder.service?.sendFetchMessages(room.messages.value.first().id,room.info.value.id)
            }
        } else secondTime = true
    }

    /**
     * Method that returns if user is owner of picked chat room
     * @return True if user is owner otherwise false
     */
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

    /**
     * Method that sends delete room message to server
     */
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

        /**
         * Static method that creates custom vm factory for [AllChatsVM] viewModel with custom parameters.
         * @param context  Application context for symbol rendering and binding to service.
         */
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