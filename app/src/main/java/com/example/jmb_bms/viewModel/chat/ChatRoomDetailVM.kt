package com.example.jmb_bms.viewModel.chat

import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.jmb_bms.connectionService.models.UserProfile
import com.example.jmb_bms.model.LivePickedRoomDetail
import com.example.jmb_bms.model.LiveServiceState
import com.example.jmb_bms.model.LiveUsers
import com.example.jmb_bms.model.database.chat.ChatDBHelper
import com.example.jmb_bms.viewModel.ServiceBinder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ChatRoomDetailVM(ctx: Context, chatRoomId: String) : ViewModel() {

    private val dbHelper = ChatDBHelper(ctx,null)
    val liveUsers = LiveUsers(ctx)
    val serviceState = LiveServiceState()

    val livePickedRoomDetail = LivePickedRoomDetail(chatRoomId, dbHelper, liveUsers, ctx)

    val serviceBinder = ServiceBinder(ctx, listOf(liveUsers,serviceState,livePickedRoomDetail))

    val profile = serviceBinder.service?.serviceModel?.profile

    //val pickedUsers = mutableListOf<String>()
    fun userIsOwner() = profile?.serverId == livePickedRoomDetail.pickedRoom.value?.info?.value?.owner

    override fun onCleared() {
        super.onCleared()
        dbHelper.close()
        liveUsers.onDestroy()
        serviceBinder.unbind()
    }

    companion object{
        fun create(ctx: Context, chatRoomId: String): ViewModelProvider.Factory{

            return object : ViewModelProvider.Factory{
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    if( modelClass.isAssignableFrom(ChatRoomDetailVM::class.java) )
                    {
                        return ChatRoomDetailVM( ctx, chatRoomId) as T
                    }
                    throw IllegalArgumentException("Unknown VM class")
                }
            }
        }
    }

}