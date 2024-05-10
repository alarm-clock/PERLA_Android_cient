/**
 * @file ServerScreen.kt
 * @author Jozef Michal Bukas <xbukas00@stud.fit.vutbr.cz,jozefmbukas@gmail.com>
 * Description: File containing composable functions for server screen
 */
package com.example.jmb_bms.view.server

import android.util.Log
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.MutableLiveData
import androidx.navigation.NavHostController
import com.example.jmb_bms.connectionService.ConnectionState
import com.example.jmb_bms.connectionService.models.UserProfile
import com.example.jmb_bms.model.TeamLiveDataHolder
import com.example.jmb_bms.ui.theme.*
import com.example.jmb_bms.view.BottomBar1
import com.example.jmb_bms.view.MenuTop1
import com.example.jmb_bms.viewModel.LiveLocationFromPhone
import com.example.jmb_bms.viewModel.LiveTime
import com.example.jmb_bms.viewModel.server.ServerVM

/**
 * User row
 *
 * @param profile
 * @param serverVM
 * @param teamLeadVersion
 * @param isTeamLead
 * @param showUserLocSh
 */
@Composable
fun UserRow(profile: MutableLiveData<UserProfile>, serverVM: ServerVM, teamLeadVersion: Boolean = false, isTeamLead: Boolean = false, showUserLocSh: Boolean = true)
{
    val prof by profile.observeAsState()
    var showMenu by remember { mutableStateOf(false) }

    var userName = prof?.userName ?: ""
    var isClient =  prof != null && prof?.serverId == serverVM.userProfile?.serverId
    if(isClient)
    {
        userName = "You"
    }

    val scheme = LocalTheme.current
    val menuItemColors = LocalMenuItemsTheme.current

    Row(
        modifier = Modifier.padding(5.dp).fillMaxWidth().clickable(enabled = prof?.location != null){ serverVM.showUserInLocusMap(prof)},
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Spacer(Modifier.width(1.dp))
        Text(userName, fontSize = 25.sp, color = scheme.onPrimary)

        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ){
            if((prof?.location != null) && showUserLocSh)
            {
                Icon(Icons.Default.Map,"", tint = scheme.onPrimary)
            }
            if(isTeamLead)
            {
                Icon(Icons.Default.Star,"",tint = scheme.onPrimary)
            }
            if(prof?.symbol != null && prof?.symbol?.imageBitmap != null)
            {
                Image(prof?.symbol?.imageBitmap!!,null)
            }
            if(teamLeadVersion)
            {
                Icon(Icons.Default.Menu,"", modifier = Modifier.clickable { showMenu = true },tint = scheme.onPrimary)

                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = {showMenu = false},
                    Modifier.background(scheme.primary).border(1.dp,scheme.outline,RoundedCornerShape(6.dp))
                ){
                    DropdownMenuItem(
                        text = { Text("Kick from team", color = Color(50,0,0)) },
                        onClick = {serverVM.kickUserFromTeam(prof!!.serverId); showMenu = false},
                        colors = menuItemColors
                    )
                    com.example.jmb_bms.view.Divider(scheme.onPrimary,0.2.dp)
                    DropdownMenuItem(
                        text = { Text("Toggle location sharing") },
                        onClick = { serverVM.toggleUsersLocationShare(prof!!.serverId);showMenu = false},
                        colors = menuItemColors
                    )
                    if(!isClient) {
                        com.example.jmb_bms.view.Divider(scheme.onPrimary,0.2.dp)
                        DropdownMenuItem(
                            text = { Text("Make user team leader") },
                            onClick = { serverVM.makeUserTeamLead(prof!!.serverId); showMenu = false },
                            colors = menuItemColors
                        )
                    }
                }
            }
        }

    }
}

/**
 * Print connected users
 *
 * @param serverVM
 */
@Composable
fun PrintConnectedUsers(serverVM: ServerVM)
{
    val userList by serverVM.connectedUsers.collectAsState()

    LazyColumn {
        itemsIndexed(userList) { index, item ->
            UserRow(item,serverVM)
            if(index != userList.lastIndex)
            {
                com.example.jmb_bms.view.Divider(LocalTheme.current.onPrimary, 2.dp)
            }
        }
    }
}

/**
 * Refresh rate row
 *
 * @param serverVM
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RefreshRateRow(serverVM: ServerVM)
{
    var expanded by remember { mutableStateOf(false) }
    var value by serverVM.pickedRefresh

    val scheme = LocalTheme.current
    val menuItemColors = LocalMenuItemsTheme.current
    val textFieldColors = LocalTextfieldTheme.current ?: TextFieldDefaults.colors()

    Row(verticalAlignment = Alignment.CenterVertically) {
        Text("Location share refresh rate: ", fontSize = 20.sp, color =  scheme.onPrimary)
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = {expanded = !expanded}
        ){
            TextField(
                modifier = Modifier.menuAnchor().fillMaxWidth().border(0.5.dp,if(isSystemInDarkTheme()) darkCianColor else Color.Transparent),
                readOnly = true,
                value = value.menuString,
                onValueChange = {},
                label = { Text("Refresh rate") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                colors = textFieldColors,
            )
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = {expanded = false},
                Modifier.background(scheme.primary).border(1.dp,scheme.outline,RoundedCornerShape(6.dp))
            ){
                serverVM.refreshValues.forEach{
                    DropdownMenuItem(
                        text = { Text(it.menuString) },
                        onClick = {
                            value = it
                            expanded = false
                            serverVM.selectOption(it)
                        },
                        contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding,
                        colors = menuItemColors
                    )
                    com.example.jmb_bms.view.Divider(scheme.onPrimary,0.2.dp)
                }
            }
        }
    }
}

/**
 * Row that shows connection state
 *
 * @param connectionState
 * @param errMsg
 */
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
    val scheme = LocalTheme.current

    val color = when(connectionState)
    {
        null -> scheme.error
        ConnectionState.NOT_CONNECTED -> scheme.error
        ConnectionState.ERROR -> scheme.error
        ConnectionState.NEGOTIATING -> if(isSystemInDarkTheme()) darkOrange else Color.Yellow
        ConnectionState.CONNECTED -> if(isSystemInDarkTheme()) darkCianColor else  Color.Green
        else -> scheme.error
    }

    Row(
        modifier = Modifier.fillMaxWidth().background(color),
        horizontalArrangement = Arrangement.Center
    ) {
        Text(
            text = txt, fontSize = 35.sp, color = Color.Black
        )
    }
    if(connectionState == ConnectionState.ERROR)
    {
        Text(text = errMsg ?: "",color = Color.Red, fontSize = 15.sp)
    }
}

/**
 * Custom switch
 *
 * @param checked
 * @param text
 * @param enabled
 * @param fontSize
 * @param onCheckedChange
 */
@Composable
fun CustomSwitch(checked: Boolean, text: String, enabled: Boolean = true, fontSize: TextUnit = 30.sp, onCheckedChange: (Boolean) -> Unit)
{
    val scheme = LocalTheme.current
    val switchColors = LocalSwitchTheme.current ?: SwitchDefaults.colors()

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = text, fontSize = fontSize, color = scheme.onPrimary)
        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .width(1.dp)
                    .height(25.dp)
                    .background(scheme.onPrimary)
            )
            Switch(
                modifier = Modifier.size(70.dp),
                checked = checked,
                onCheckedChange = onCheckedChange,
                enabled = enabled,
                colors = switchColors
            )
        }
    }
}

/**
 * Print team members
 *
 * @param teamProfile
 * @param serverVM
 */
@Composable
fun PrintTeamMembers(teamProfile: TeamLiveDataHolder, serverVM: ServerVM)
{
    val members by teamProfile.pair.second.collectAsState() //by teamProfile.value.pair.second.collectAsState()
    val profile by teamProfile.pair.first.observeAsState()

    Box(modifier = Modifier.clip(RoundedCornerShape(16.dp)).background(LocalTheme.current.wrappingBox).fillMaxSize()) {
        LazyColumn {
            items(members.toList()) {
                UserRow(it, serverVM,
                    serverVM.userProfile!!.serverId == profile!!.teamLead ,
                    it.value!!.serverId == profile!!.teamLead
                )
                com.example.jmb_bms.view.Divider(LocalTheme.current.onPrimary, 1.dp)
            }
        }
    }

}


/**
 * Print teams
 *
 * @param serverVM
 * @param navHostController
 */
@Composable
fun PrintTeams(serverVM: ServerVM, navHostController: NavHostController)
{
    val menuItems by serverVM.teams.data.collectAsState()

    LazyColumn {
        items(menuItems.toList()){
            TeamInfoRow(it,{}){
                Log.d("Row","In on button click")
                serverVM.selectTeamOption(it)
                navHostController.navigate("TeamDetail/${it.getTeamId()}")
            }
            com.example.jmb_bms.view.Divider(LocalTheme.current.onPrimary, 1.dp)
        }
    }
}

/**
 * Print connected users or teams
 *
 * @param serverVM
 * @param navHostController
 */
@Composable
fun PrintConnectedUsersOrTeams(serverVM: ServerVM, navHostController: NavHostController)
{
    var checked by remember { mutableStateOf(false) }
    val scheme = LocalTheme.current

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        Button(
            onClick = { checked = false },
            enabled = checked,
            colors = ButtonColors(scheme.pickOneBtnFromManyEn, scheme.pickOneBtnFromManyInsideEn, scheme.pickOneBtnFromManyDis, scheme.pickOneBtnFromManyInsideDis),
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(16.dp,0.dp,0.dp,16.dp),
            border = BorderStroke(1.dp,if(isSystemInDarkTheme()) darkCianColor else Color.Transparent)
        ){
            Text("Users", fontSize =  20.sp)
        }
        Button(
            onClick = { checked = true },
            enabled = !checked,
            colors = ButtonColors(scheme.pickOneBtnFromManyEn, scheme.pickOneBtnFromManyInsideEn, scheme.pickOneBtnFromManyDis, scheme.pickOneBtnFromManyInsideDis),
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(0.dp,16.dp,16.dp,0.dp),
            border = BorderStroke(1.dp,if(isSystemInDarkTheme()) darkCianColor else Color.Transparent)
        ){
            Text("Teams", fontSize = 20.sp)
        }
    }

        Box(modifier = Modifier.clip(RoundedCornerShape(16.dp)).background(scheme.wrappingBox).fillMaxSize()) {
            if(!checked)
            {
                PrintConnectedUsers(serverVM)
            } else
            {
                PrintTeams(serverVM,navHostController)
            }
        }

}

/**
 * Server screen buttons and data
 *
 * @param serverVM
 * @param padding
 * @param navHostController
 */
@Composable
fun ServerScreenButtonsAndData(serverVM: ServerVM, padding: PaddingValues, navHostController: NavHostController)
{
    val connectionState by serverVM.connectionState.observeAsState()
    val sharingLocation by serverVM.sharingLocation.observeAsState()
    val errMsg by serverVM.connectionErrorMsg.observeAsState()
    val checked by serverVM.checked

    val scheme = LocalTheme.current

    Column(
        modifier = Modifier.padding(padding).fillMaxSize().background(scheme.primary),
    ) {
        StateShow(connectionState,errMsg)

        CustomSwitch(checked,"Connect to server" ){
            serverVM.toggleCheck()
            serverVM.changeConnnectionState()
        }
        com.example.jmb_bms.view.Divider(scheme.onPrimary, 1.dp)
        CustomSwitch(sharingLocation ?: false,"Location sharing",connectionState == ConnectionState.CONNECTED){
            if( sharingLocation != null && sharingLocation == true)
            {
                serverVM.stopSharingLocation()
            } else
            {
                serverVM.startSharingLocation()
            }
        }
        com.example.jmb_bms.view.Divider(scheme.onPrimary, 1.dp)
        if(sharingLocation == true)
        {
            RefreshRateRow(serverVM)
            com.example.jmb_bms.view.Divider(Color.Black, 1.dp)
        }
        if(connectionState == ConnectionState.CONNECTED)
        {
            PrintConnectedUsersOrTeams(serverVM,navHostController)
        } else
        {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ){
                Text("Not connected so no users to show :(", fontSize = 15.sp, color = scheme.onPrimary)
            }

        }
    }
}

/**
 * Server screen
 *
 * @param currLoc
 * @param currTime
 * @param serverVM
 * @param navHostController
 * @param backHandler
 * @param changeScreen
 * @receiver
 * @receiver
 */
@Composable
fun ServerScreen(currLoc: LiveLocationFromPhone, currTime: LiveTime, serverVM: ServerVM, navHostController: NavHostController, backHandler: () -> Unit, changeScreen: () -> Unit)
{

    TestTheme {
        val scheme = LocalTheme.current
        Scaffold(
            topBar = { MenuTop1(currTime,currLoc) },
            bottomBar = {
                BottomBar1(
                    rButtonText = "Edit server information",
                    rButtonIcon = null,
                    rButtonStateColor = ButtonColors(scheme.secondary,scheme.onSecondary,scheme.secondary,scheme.disabledButton),
                    backButtonLogic = backHandler)
                {
                    //Log.d("ServerScreen","edit info button")
                    serverVM.stopSharingLocation()
                    Log.d("HERE8","HERE8")
                    serverVM.disconnect()
                    Log.d("ServerScreen","In edit connection info button")
                    changeScreen()
                }
            }
        ) { padding ->
            ServerScreenButtonsAndData(serverVM, padding, navHostController)
        }
    }
}