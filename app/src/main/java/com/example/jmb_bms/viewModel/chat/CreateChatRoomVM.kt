package com.example.jmb_bms.viewModel.chat

import android.annotation.SuppressLint
import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.jmb_bms.model.LiveServiceState
import com.example.jmb_bms.model.LiveUsers
import com.example.jmb_bms.model.ServerInfo
import com.example.jmb_bms.viewModel.ServiceBinder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

class CreateChatRoomVM(appCtx: Context): ViewModel(){

    val liveUsers = LiveUsers(appCtx)

    val liveServiceState = LiveServiceState()

    private val serviceBinder = ServiceBinder(appCtx, listOf(liveUsers,liveServiceState))

    private var _trimmedName: String? = null
    val chatName = MutableLiveData("")

    val pickedUsersList = mutableListOf<String>()

    val canCreateRoom = MutableStateFlow(false)

    val loading = MutableStateFlow(false)

    private fun checkAndReformatValue(newAddress: String): String?
    {
        var trimmed = newAddress.trim()
        trimmed = trimmed.replace(' ','-')
        return if(trimmed.contains(' ')) null
        else trimmed
    }

    fun editName(newName: String)
    {
        chatName.value = newName

        viewModelScope.launch(Dispatchers.IO){
            val trimmed = checkAndReformatValue(newName)
            _trimmedName = trimmed
            CoroutineScope(Dispatchers.Main).launch {
                canCreateRoom.value = !(trimmed == null || trimmed == "")
            }
        }
    }

    fun createChatRoom()
    {
        loading.value = true
        if(canCreateRoom.value)
        {
            serviceBinder.service?.createChatRoom(_trimmedName!!,pickedUsersList)
        }
    }


    override fun onCleared() {
        super.onCleared()
        liveUsers.onDestroy()
        serviceBinder.unbind()
    }

    companion object{
        fun create(context: Context): ViewModelProvider.Factory{

            return object : ViewModelProvider.Factory{
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    if( modelClass.isAssignableFrom(CreateChatRoomVM::class.java) )
                    {
                        return CreateChatRoomVM( context) as T
                    }
                    throw IllegalArgumentException("Unknown VM class")
                }
            }
        }
    }

}