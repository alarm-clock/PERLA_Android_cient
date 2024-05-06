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
import com.example.jmb_bms.model.LiveServiceState
import com.example.jmb_bms.model.LiveUsers
import com.example.jmb_bms.viewModel.ServiceBinder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel that holds data and implements method for chat room creating.
 * @param appCtx Application context for binding to service
 */
class CreateChatRoomVM(appCtx: Context): ViewModel(){

    val liveUsers = LiveUsers(appCtx)

    val liveServiceState = LiveServiceState()

    private val serviceBinder = ServiceBinder(appCtx, listOf(liveUsers,liveServiceState))

    private var _trimmedName: String? = null
    val chatName = MutableLiveData("")

    val pickedUsersList = mutableListOf<String>()

    val canCreateRoom = MutableStateFlow(false)

    val loading = MutableStateFlow(false)

    /**
     * Method that checks and reformats passed string by replacing spaces with '-'
     * @param newStr String for checking and reformatting
     */
    private fun checkAndReformatValue(newStr: String): String?
    {
        var trimmed = newStr.trim()
        trimmed = trimmed.replace(' ','-')
        return if(trimmed.contains(' ')) null
        else trimmed
    }

    /**
     * Method that edit live [chatName], checks if new value is correct and stores it in [_trimmedName]
     * @param newName New chat room name
     */
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

    /**
     * Method that sends create chat room message to server if chat room can be created
     */
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

        /**
         * Static method that creates custom vm factory for [CreateChatRoomVM] viewModel with custom parameters
         * @param context Application context for binding to service
         */
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