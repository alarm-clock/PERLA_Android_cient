package com.example.jmb_bms.viewModel.server

import androidx.lifecycle.MutableLiveData
import com.example.jmb_bms.model.ServerEditingIconModel
import com.example.jmb_bms.model.icons.SymbolCreationVMHelper
import kotlinx.coroutines.Dispatchers
import kotlin.coroutines.CoroutineContext

class TeamEditingVM(val serverVM: ServerVM, val scopeLaunch: (context: CoroutineContext, code: suspend () -> Unit) -> Unit)  {

    val model = ServerEditingIconModel(serverVM.applicationContext)

    val teamName = MutableLiveData<String>()
    var _teamName = ""
    var nameCorrect = true

    val symbolCreationVMHelper = SymbolCreationVMHelper(model,everyThingEntered = null,false,scopeLaunch)

    private fun checkAndReformatValue(string: String): String?
    {
        val trimmed = string.trim()

        return if(trimmed.contains(' ')) null
        else trimmed
    }
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

    fun sendUpDatedValuesToServer()
    {
        val name = if(_teamName != "") _teamName else serverVM.pickedTeam.value.pair.first.value?.teamName ?: return
        val iconString = symbolCreationVMHelper.model.symbolString

        if( name == "" && iconString == "") return
        if( !nameCorrect ) return //TODO print some error to user that he is mentally retarded or something

        serverVM.updateTeamNameAndIcon(name, if(iconString != "") iconString else serverVM.pickedTeam.value.pair.first.value?.teamIcon ?: return )
        reset()
    }

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