package com.example.jmb_bms.model

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.jmb_bms.connectionService.in_app_communication.LiveUsersCallback
import com.example.jmb_bms.connectionService.models.UserProfile
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.concurrent.thread

class LiveUsers: LiveUsersCallback {

    val connectedUsers = MutableStateFlow(listOf<MutableLiveData<UserProfile>>())

    override fun updatedUserListCallBack(newList: List<UserProfile>) {

        val list = newList.map { profile ->
            connectedUsers.value.find { it.value?.serverId == profile.serverId } ?: MutableLiveData(profile)
        }

        CoroutineScope(Dispatchers.Main).launch {
            connectedUsers.value = list
            Log.d("ServerVM","Setting connectedUsers list: newList is $list")
        }
    }
    override fun updateUserList(profile: UserProfile, add: Boolean) {

        val tmp = connectedUsers.value.toMutableList()
        if(add) tmp.add(MutableLiveData(profile))
        else tmp.removeIf { it.value?.serverId == profile.serverId }

        CoroutineScope(Dispatchers.Main).launch {
            connectedUsers.value = tmp
        }
    }

    override fun profileChanged(profile: UserProfile) {
        val liveProfile = connectedUsers.value.find { it.value?.serverId == profile.serverId }
        if(liveProfile == null)
        {
            Log.d("ServerVM", "Somehow there is not stored profile which changed")
            return
        }
        liveProfile.postValue(profile.copy())
    }
}