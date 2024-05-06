/**
 * @file: ServerInfoVM.kt
 * @author: Jozef Michal Bukas <xbukas00@stud.fit.vutbr.cz,jozefmbukas@gmail.com>
 * Description: File containing ServerInfoVM class
 */
package com.example.jmb_bms.viewModel.server

import androidx.lifecycle.MutableLiveData
import com.example.jmb_bms.model.ServerEditingIconModel
import com.example.jmb_bms.model.icons.SymbolCreationVMHelper
import kotlinx.coroutines.Dispatchers
import kotlin.coroutines.CoroutineContext

/**
 * Class that holds data required to update point name and icon
 * @param serverVM ViewModel of given screen
 * @param scopeLaunch
 */
class TeamEditingVM(val serverVM: ServerVM, val scopeLaunch: (context: CoroutineContext, code: suspend () -> Unit) -> Unit)  {

    val model = ServerEditingIconModel(serverVM.applicationContext)

    val teamName = MutableLiveData<String>()
    var _teamName = ""
    var nameCorrect = true

    val symbolCreationVMHelper = SymbolCreationVMHelper(model,everyThingEntered = null,false,scopeLaunch)

    /**
     * Method that checks and trims string
     * @param string string that will be checked and trimmed
     * @return Null if [string] has any space between works otherwise trimmed string
     */
    private fun checkAndReformatValue(string: String): String?
    {
        val trimmed = string.trim()

        return if(trimmed.contains(' ')) null
        else trimmed
    }

    /**
     * Method that updates team name by new string and checks if it is valid
     * @param newName
     */
    fun updateTeamName(newName: String)
    {
        scopeLaunch(Dispatchers.IO)
        {
            teamName.postValue(newName)

            val trimmed = checkAndReformatValue(newName)
            if( trimmed == null && newName != "")
            {
                nameCorrect = false
            } else
            {
                _teamName = newName
                nameCorrect = true
            }
        }
    }

    /**
     * Method that sends updated values to server. If no new values were specified then current team values will be used.
     * After that [reset] method is invoked.
     */
    fun sendUpDatedValuesToServer()
    {
        val name = if(_teamName != "") _teamName else serverVM.pickedTeam.value.pair.first.value?.teamName ?: return
        val iconString = symbolCreationVMHelper.model.symbolString

        if( name == "" && iconString == "") return
        if( !nameCorrect ) return //TODO print some error to user that he is mentally ******** or something

        serverVM.updateTeamNameAndIcon(name, if(iconString != "") iconString else serverVM.pickedTeam.value.pair.first.value?.teamIcon ?: return )
        reset()
    }

    /**
     * Method that resets state of this object
     */
    fun reset()
    {
        scopeLaunch(Dispatchers.IO)
        {
            teamName.postValue("")
            _teamName = ""

            symbolCreationVMHelper.reset()
        }
    }
}