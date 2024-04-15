package com.example.jmb_bms.viewModel.server

import androidx.compose.runtime.mutableStateListOf
import com.example.jmb_bms.model.utils.AddingScreenTuple
import kotlinx.coroutines.Dispatchers
import kotlin.coroutines.CoroutineContext

class AddingUsersToTeamVM(val serverVM: ServerVM, val scopeLaunch: (context: CoroutineContext, code: suspend () -> Unit) -> Unit) {

    val pickedUserIds = mutableStateListOf<String>()
    var list : List<AddingScreenTuple>? = null
    private var pickedTeamId : String? = null
    fun prepare()
    {
        scopeLaunch(Dispatchers.IO)
        {
            pickedTeamId = serverVM.pickedTeam.value.getTeamId() ?: return@scopeLaunch
            list = serverVM.getUsersWhichAreNotOnTeam(pickedTeamId!!).map {
                AddingScreenTuple(it)
            }
        }
    }
    fun reset() {
        list = null
        pickedTeamId = null
        pickedUserIds.removeIf { true }
    }

    fun addUsers()
    {
        scopeLaunch(Dispatchers.IO)
        {
            if(pickedUserIds.isEmpty())
            {
                reset()
                return@scopeLaunch
            }
            pickedUserIds.forEach {
                serverVM.addUserToTeam(pickedTeamId!!,it)
            }
            reset()
        }
    }
}