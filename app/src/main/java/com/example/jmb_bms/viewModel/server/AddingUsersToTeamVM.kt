/**
 * @file: AddingUsersToTeamVM.kt
 * @author: Jozef Michal Bukas <xbukas00@stud.fit.vutbr.cz,jozefmbukas@gmail.com>
 * Description: File containing AddingUsersToTeamVM class
 */
package com.example.jmb_bms.viewModel.server

import androidx.compose.runtime.mutableStateListOf
import com.example.jmb_bms.model.utils.AddingScreenTuple
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext

/**
 * Class which holds data and methods for screen where user picks multiple users that will be added to team
 * @param serverVM ViewModel for given screen
 */
class AddingUsersToTeamVM(val serverVM: ServerVM) {

    val pickedUserIds = mutableStateListOf<String>()
    var list : List<AddingScreenTuple>? = null
    private var pickedTeamId : String? = null

    /**
     * Method which prepares [pickedTeamId] and [list] attributes before picking users
     */
    fun prepare()
    {
        CoroutineScope(Dispatchers.IO).launch {
            pickedTeamId = serverVM.pickedTeam.value.getTeamId() ?: return@launch
            list = serverVM.getUsersWhichAreNotOnTeam(pickedTeamId!!).map {
                AddingScreenTuple(it)
            }
        }
    }

    /**
     * Method that resets [list], [pickedTeamId], and [pickedUserIds]
     */
    fun reset() {
        list = null
        pickedTeamId = null
        pickedUserIds.removeIf { true }
    }

    /**
     * Method which calls viewModel method for adding users to team for each id stored in [pickedUserIds] list.
     * Then it resets this object by invoking [reset].
     */
    fun addUsers()
    {
        CoroutineScope(Dispatchers.IO).launch {
            if(pickedUserIds.isEmpty())
            {
                reset()
                return@launch
            }
            pickedUserIds.forEach {
                serverVM.addUserToTeam(pickedTeamId!!,it)
            }
            reset()
        }
    }
}