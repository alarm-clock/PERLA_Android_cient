package com.example.jmb_bms.view

import android.content.Context
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.edit
import androidx.lifecycle.MutableLiveData
import com.example.jmb_bms.connectionService.ConnectionState
import com.example.jmb_bms.connectionService.UserProfile
import com.example.jmb_bms.viewModel.LiveLocationFromLoc
import com.example.jmb_bms.viewModel.LiveTime
import com.example.jmb_bms.viewModel.ServerVM

@Composable
fun UserRow(profile: MutableLiveData<UserProfile>)
{
    val prof by profile.observeAsState()
    Row {
        Text(prof?.userName ?: "")
        if(prof?.symbol != null && prof?.symbol?.imageBitmap != null)
        {
            Image(prof?.symbol?.imageBitmap!!,null)
        }
    }
}

@Composable
fun PrintConnectedUsers(serverVM: ServerVM)
{
    val userList by serverVM.connectedUsers.collectAsState()

    Column () {

        userList.forEachIndexed { index, it ->
            UserRow(it)
            if(index != userList.lastIndex)
            {
                Divider(Color.Black,2.dp)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RefreshRateRow(serverVM: ServerVM)
{
    var expanded by remember { mutableStateOf(false) }
    var value by serverVM.pickedRefresh

    Row {
        Text("Location share refresh rate: ", fontSize = 20.sp)
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = {expanded = !expanded}
        ){
            TextField(
                modifier = Modifier.menuAnchor().fillMaxWidth(),
                readOnly = true,
                value = value.menuString,
                onValueChange = {},
                label = { Text("Refresh rate") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                colors = ExposedDropdownMenuDefaults.textFieldColors(),
            )
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = {expanded = false}
            ){
                serverVM.refreshValues.forEach{
                    DropdownMenuItem(
                        text = { Text(it.menuString) },
                        onClick = {
                            value = it
                            expanded = false
                            serverVM.selectOption(it)
                        },
                        contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                    )
                }
            }
        }
    }
}


@Composable
fun ServerScreenButtonsAndData(serverVM: ServerVM, padding: PaddingValues,changeScreen: () -> Unit)
{
    val connectionState by serverVM.connectionState.observeAsState()
    val sharingLocation by serverVM.sharingLocation.observeAsState()
    val context = LocalContext.current

    Column( modifier = Modifier.padding(padding) ) {
        Button(
            modifier = Modifier.fillMaxWidth(),
            onClick = {
                if( sharingLocation != null && sharingLocation == true)
                {
                    serverVM.stopSharingLocation()
                } else
                {
                    serverVM.startSharingLocation()
                }
            },
            content = {
                if( sharingLocation != null && sharingLocation == true && connectionState == ConnectionState.CONNECTED)
                {
                    Text("Stop Sharing Location")
                } else
                {
                    Text("Start sharing location")
                }
            },
            enabled = connectionState == ConnectionState.CONNECTED,
            colors =  ButtonColors(Color.Blue, Color.White, Color.Gray, Color.Black)
        )
        Button(
            modifier = Modifier.fillMaxWidth(),
            onClick = {
                serverVM.stopSharingLocation()
                serverVM.disconnect()
                changeScreen()
            },
            content = {
                Text("Edit server info")
            }
        )
        if(sharingLocation == true)
        {
            RefreshRateRow(serverVM)
        }
        if(connectionState == ConnectionState.CONNECTED)
        {
            PrintConnectedUsers(serverVM)
        } else
        {
            Text("Not connected so no users to show")
        }
    }
}



@Composable
fun ServerScreen(currLoc: LiveLocationFromLoc, currTime: LiveTime,serverVM: ServerVM,backHandler: () -> Unit, changeScreen: () -> Unit)
{
    val connectionState by serverVM.connectionState.observeAsState()
    Log.d("ServerScreen", "Connection state is $connectionState")

    Scaffold(
        topBar = { MenuTop2(currTime,currLoc,backHandler){} },
        bottomBar = {
            Button(
                enabled =  true,    //changethis
                modifier = Modifier.fillMaxWidth(),
                onClick = {
                    serverVM.changeConnnectionState()
                },
                colors = ButtonColors(Color.Blue, Color.White, Color.Gray, Color.Black),
                content = {
                    if(connectionState == ConnectionState.NOT_CONNECTED || connectionState == ConnectionState.ERROR)
                    {
                        Text("Reconnect to server")
                    } else
                    {
                        Text("Disconnect")
                    }
                }
            )
        }
    ) { padding ->
        ServerScreenButtonsAndData(serverVM, padding, changeScreen)
    }

}