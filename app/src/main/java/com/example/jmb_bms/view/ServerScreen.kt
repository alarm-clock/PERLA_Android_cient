package com.example.jmb_bms.view

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.MutableLiveData
import com.example.jmb_bms.connectionService.ConnectionState
import com.example.jmb_bms.connectionService.models.TeamProfile
import com.example.jmb_bms.connectionService.models.UserProfile
import com.example.jmb_bms.model.TeamLiveDataHolder
import com.example.jmb_bms.viewModel.LiveLocationFromLoc
import com.example.jmb_bms.viewModel.LiveTime
import com.example.jmb_bms.viewModel.ServerVM
import locus.api.android.utils.LocusUtils


@Composable
fun UserRow(profile: MutableLiveData<UserProfile>, serverVM: ServerVM ,teamLeadVersion: Boolean = false, isTeamLead: Boolean = false)
{
    val prof by profile.observeAsState()
    var showMenu by remember { mutableStateOf(false) }

    var userName = prof?.userName ?: ""
    if( prof != null && prof?.serverId == serverVM.userProfile?.serverId)
    {
        userName = "You"
    }

    Row(
        modifier = Modifier.fillMaxWidth().clickable(enabled = prof?.location != null){ serverVM.showUserInLocusMap(prof)},
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Spacer(Modifier.width(1.dp))
        Text(userName, fontSize = 25.sp)

        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ){
            if(prof?.location != null)
            {
                Icon(Icons.Default.Map,"")
            }
            if(isTeamLead)
            {
                Icon(Icons.Default.Star,"")
            }
            if(prof?.symbol != null && prof?.symbol?.imageBitmap != null)
            {
                Image(prof?.symbol?.imageBitmap!!,null)
            }
            if(teamLeadVersion)
            {
                Icon(Icons.Default.Menu,"", modifier = Modifier.clickable { showMenu = true /* TODO team lead menu, location, delete atd. */ })

                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = {showMenu = false}
                ){
                    DropdownMenuItem(
                        text = { Text("Kick from team") },
                        onClick = {serverVM.kickUserFromTeam(prof!!.serverId); showMenu = false}
                    )
                    DropdownMenuItem(
                        text = { Text("Toggle location sharing") },
                        onClick = { serverVM.toggleUsersLocationShare(prof!!.serverId);showMenu = false}
                    )
                    DropdownMenuItem(
                        text = { Text("Make user team leader") },
                        onClick = { serverVM.makeUserTeamLead(prof!!.serverId)  ; showMenu = false}
                    )
                }
            }
        }

    }
}

@Composable
fun PrintConnectedUsers(serverVM: ServerVM)
{
    val userList by serverVM.connectedUsers.collectAsState()

    LazyColumn {
        itemsIndexed(userList){ index, item ->
            UserRow(item,serverVM)
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
fun StateShow(connectionState: ConnectionState?, errMsg: String?)
{
    val txt = when(connectionState){
        null -> "Disconnected"
        ConnectionState.NOT_CONNECTED -> "Disconnected"
        ConnectionState.ERROR -> "Disconnected"
        ConnectionState.NEGOTIATING -> "Connecting..."
        ConnectionState.CONNECTED -> "Connected"
        else -> "Disconnected"
    }

    val color = when(connectionState)
    {
        null -> Color.Red
        ConnectionState.NOT_CONNECTED -> Color.Red
        ConnectionState.ERROR -> Color.Red
        ConnectionState.NEGOTIATING -> Color.Yellow
        ConnectionState.CONNECTED -> Color.Green
        else -> Color.Red
    }

    Row(
        modifier = Modifier.fillMaxWidth().background(color),
        horizontalArrangement = Arrangement.Center
    ) {
        Text(
            text = txt, fontSize = 35.sp
        )
    }
    if(connectionState == ConnectionState.ERROR)
    {
        Text(text = errMsg ?: "",color = Color.Red, fontSize = 15.sp)
    }
}

@Composable
fun CustomSwitch(checked: Boolean, text: String, enabled: Boolean = true, fontSize: TextUnit = 30.sp, onCheckedChange: (Boolean) -> Unit)
{
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = text, fontSize = fontSize)
        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .width(1.dp)
                    .height(25.dp)
                    .background(Color.DarkGray)
            )
            Switch(
                modifier = Modifier.size(70.dp),
                checked = checked,
                onCheckedChange = onCheckedChange,
                enabled = enabled
            )
        }
    }
}

@Composable
fun PrintTeamMembers(teamProfile: TeamLiveDataHolder, serverVM: ServerVM)
{
    val members by teamProfile.pair.second.collectAsState() //by teamProfile.value.pair.second.collectAsState()
    val profile by teamProfile.pair.first.observeAsState()

    Log.d("PrintTeamMembers","Drawn...")
    LazyColumn {

        itemsIndexed(members.toList()){ index, it ->
            UserRow(it, serverVM,
                serverVM.userProfile!!.serverId == profile!!.teamLead ,//teamProfile.pair.first.value!!.teamLead,
                it.value!!.serverId == profile!!.teamLead
            )
            Divider(Color.Black, 1.dp)
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrintTeams(serverVM: ServerVM)
{
    var expanded by remember { mutableStateOf(false) }
    var picked by remember { mutableStateOf(false) }
    var value by serverVM.pickedTeam
    val menuItems by serverVM.teams.data.collectAsState()

        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded }
        ) {
            TextField(
                modifier = Modifier.menuAnchor().fillMaxWidth(),
                readOnly = true,
                value = value.pair.first.value!!.teamName,
                onValueChange = {},
                label = { Text("Team") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                colors = ExposedDropdownMenuDefaults.textFieldColors(),
            )
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = {
                    expanded = false
                    if (value.pair.first.value?._id != "") picked = true
                }
            ) {
                menuItems.forEach {
                    val text = it.pair.first.value?.teamName

                    if (text != null) {
                        DropdownMenuItem(
                            text = { Text(text) },
                            onClick = {
                                value = it
                                expanded = false
                                picked = true
                                serverVM.selectTeamOption(it)
                            },
                            contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding,
                            trailingIcon = { Image(it.pair.first.value!!.teamSymbol.imageBitmap!!, "") }
                        )
                    }
                }
            }
        }

    if(value.pair.first.value?._id != "")
    {
        PrintTeamMembers(value,serverVM)
    }
}

@Composable
fun PrintConnectedUsersOrTeams(serverVM: ServerVM)
{
    var checked by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        Button(
            onClick = { checked = false },
            enabled = checked,
            colors = ButtonColors(Color.Blue, Color.White, Color.Cyan, Color.White),
            modifier = Modifier.weight(1f).clip(RoundedCornerShape(0.dp)),
            shape = RectangleShape
        ){
            Text("Users", fontSize =  20.sp)
        }
        Button(
            onClick = { checked = true },
            enabled = !checked,
            colors = ButtonColors(Color.Blue, Color.White, Color.Cyan, Color.White),
            modifier = Modifier.weight(1f).clip(RoundedCornerShape(0.dp)),
            shape = RectangleShape
        ){
            Text("Teams", fontSize = 20.sp)
        }
    }
    if(!checked)
    {
        PrintConnectedUsers(serverVM)
    } else
    {
        PrintTeams(serverVM)
    }
}


@Composable
fun ServerScreenButtonsAndData(serverVM: ServerVM, padding: PaddingValues)
{
    val connectionState by serverVM.connectionState.observeAsState()
    val sharingLocation by serverVM.sharingLocation.observeAsState()
    val errMsg by serverVM.connectionErrorMsg.observeAsState()
    val checked by serverVM.checked

    Column(
        modifier = Modifier.padding(padding).fillMaxSize(),
    ) {
        StateShow(connectionState,errMsg)

        CustomSwitch(checked,"Connect to server" ){
            serverVM.toggleCheck()
            serverVM.changeConnnectionState()
        }
        Divider(Color.Black,1.dp)
        CustomSwitch(sharingLocation ?: false,"Location sharing",connectionState == ConnectionState.CONNECTED){
            if( sharingLocation != null && sharingLocation == true)
            {
                serverVM.stopSharingLocation()
            } else
            {
                serverVM.startSharingLocation()
            }
        }
        Divider(Color.Black,1.dp)
        if(sharingLocation == true)
        {
            RefreshRateRow(serverVM)
            Divider(Color.Black,1.dp)
        }
        if(connectionState == ConnectionState.CONNECTED)
        {
            PrintConnectedUsersOrTeams(serverVM)
        } else
        {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ){
                Text("Not connected so no users to show :(", fontSize = 15.sp)
            }

        }
    }
}

@Composable
fun ServerScreen(currLoc: LiveLocationFromLoc, currTime: LiveTime,serverVM: ServerVM,backHandler: () -> Unit, changeScreen: () -> Unit)
{
    val connectionState by serverVM.connectionState.observeAsState()
    Log.d("ServerScreen", "Connection state is $connectionState")

    Scaffold(
        topBar = { MenuTop1(currTime,currLoc) },
        bottomBar = {
            BottomBar1(
                rButtonText = "Edit server information",
                rButtonIcon = null,
                rButtonStateColor = ButtonColors(Color.Blue,Color.White,Color.Gray,Color.White),
                backButtonLogic = backHandler,
            ){
                serverVM.stopSharingLocation()
                serverVM.disconnect()
                Log.d("ServerScreen","In edit connection info button")
                changeScreen()
            }
        }
    ) { padding ->
        ServerScreenButtonsAndData(serverVM, padding)
    }

}