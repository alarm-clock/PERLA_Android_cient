package com.example.jmb_bms.view

import android.util.Log
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.sp
import com.example.jmb_bms.connectionService.ConnectionState
import com.example.jmb_bms.viewModel.LiveLocationFromLoc
import com.example.jmb_bms.viewModel.LiveTime
import com.example.jmb_bms.viewModel.ServerVM


@Composable
fun EditingModeScreen(serverVM: ServerVM)
{
    Column {

    }
}

@Composable
fun TeamDataScreen(serverVM: ServerVM, editingMode: Boolean)
{
    if(editingMode)
    {

    } else
    {

    }
}


@Composable
fun TeamDetailScreen(currLoc: LiveLocationFromLoc, currTime: LiveTime, serverVM: ServerVM, backHandler: () -> Unit, changeScreen: () -> Unit)
{
    val pickedTeam by serverVM.pickedTeam
    var editingMode by remember{ mutableStateOf(false) }

    val text = if(pickedTeam.getTeamId() != serverVM.userProfile!!.serverId) null else {
        if(editingMode) "Finnish editing" else "Edit team information"
    }

    Scaffold(
        topBar = { MenuTop1(currTime,currLoc) },
        bottomBar = {
            BottomBar1(
                rButtonText = text,
                rButtonIcon = null,
                rButtonStateColor = ButtonColors(Color.Blue, Color.White, Color.Gray, Color.White),
                backButtonLogic = backHandler,
            ){
                if(editingMode)
                {
                    //TODO send update team to server
                    editingMode = false
                } else
                {
                    editingMode = true
                }

            }
        }
    ) { padding ->
        //ServerScreenButtonsAndData(serverVM, padding)
    }
}
