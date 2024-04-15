package com.example.jmb_bms.view.server

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.jmb_bms.connectionService.ConnectionState
import com.example.jmb_bms.connectionService.models.TeamProfile
import com.example.jmb_bms.model.TeamLiveDataHolder
import com.example.jmb_bms.model.utils.AddingScreenTuple
import com.example.jmb_bms.ui.theme.*
import com.example.jmb_bms.view.BottomBar1
import com.example.jmb_bms.view.MenuTop1
import com.example.jmb_bms.viewModel.LiveLocationFromLoc
import com.example.jmb_bms.viewModel.LiveTime
import com.example.jmb_bms.viewModel.server.ServerVM


@Composable
fun UserCheckBoxRow(serverVM: ServerVM, profile: AddingScreenTuple)
{
    val liveProfile by profile.profile.observeAsState()
    var checked by profile.selected
    val id = liveProfile?.serverId ?: ""

    Row( modifier = Modifier.padding(8.dp), verticalAlignment = Alignment.CenterVertically ) {
        Checkbox(
            checked = checked,
            onCheckedChange = {
                profile.selected.value = it
                if(checked)
                {
                    serverVM.addingUsersVM.pickedUserIds.add(id)
                } else
                {
                    serverVM.addingUsersVM.pickedUserIds.remove(id)
                }
            },
            modifier = Modifier.size(30.dp),
            colors = LocalCheckBoxTheme.current ?: CheckboxDefaults.colors()
        )
        UserRow(profile.profile,serverVM, showUserLocSh = false)
    }
}


@Composable
fun AddingUsersScreen(serverVM: ServerVM, paddingValues: PaddingValues)
{
    LazyColumn( modifier = Modifier.padding(paddingValues).background(LocalTheme.current.primary).fillMaxSize()) {
        items(serverVM.addingUsersVM.list!!){
            UserCheckBoxRow(serverVM,it)
            com.example.jmb_bms.view.Divider(LocalTheme.current.onPrimary, 1.dp)
        }
    }
}


@Composable
fun EditingModeScreen(serverVM: ServerVM, paddingValues: PaddingValues)
{
    val newName by serverVM.teamEditingVM.teamName.observeAsState()

    val scheme = LocalTheme.current
    val textFieldColors = LocalTextfieldTheme.current ?: TextFieldDefaults.colors()

    Column(Modifier.padding(paddingValues).background(scheme.primary).fillMaxSize()) {
        TextField(
            value = newName ?: "",
            onValueChange = { serverVM.teamEditingVM.updateTeamName(it) },
            label = { Text("New name", fontSize = 25.sp)},
            modifier = Modifier.fillMaxWidth().border(1.dp,if(isSystemInDarkTheme()) darkCianColor else Color.Transparent),
            colors = textFieldColors
        )
        IconCreationCascade(serverVM.teamEditingVM.symbolCreationVMHelper)
    }
}

@Composable
fun TeamInfoRow(teamLiveDataHolder: TeamLiveDataHolder, onRowClick: (() -> Unit)?, onButtonClick: (() -> Unit)?)
{
    val profile by teamLiveDataHolder.pair.first.observeAsState()
    val bitMap = profile?.teamSymbol?.imageBitmap

    val scheme = LocalTheme.current

    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth().clickable(enabled = (onRowClick != null) && (onButtonClick == null) ) {
            if (onRowClick != null) {
                onRowClick()
            }
        }
    ) {
        Row(
            modifier = Modifier.weight(1.5f).clickable(enabled = (onRowClick != null) && (onButtonClick != null)) {
                if (onRowClick != null) {
                    onRowClick()
                }
            }
        ){
            Text(profile?.teamName ?: "", fontSize = 30.sp, modifier = Modifier.padding(start = 3.dp), color = scheme.onPrimary)
        }
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.End
        ) {
            //TODO add location sharing icon before icon
            if(bitMap != null)
                Image(bitMap ,"")
            if( onButtonClick != null)
            {
                Button(
                    colors = ButtonColors(scheme.enabledButton,scheme.onSecondary, scheme.disabledButton, scheme.onSecondary),
                    border = BorderStroke(1.dp,if(isSystemInDarkTheme()) darkCianColor else Color.Transparent),
                    onClick = { onButtonClick() }
                ){
                    Text("Detail", fontSize = 15.sp)
                }
            }
        }
    }
}
@Composable
fun TeamOpsButton(serverVM: ServerVM, backHandler: () -> Unit)
{
    var showMenu by remember { mutableStateOf(false) }

    val scheme = LocalTheme.current
    val menuItemColors = LocalMenuItemsTheme.current

    Button(
        onClick = { showMenu = true},
        modifier = Modifier.fillMaxWidth(),
        border = BorderStroke(1.dp,scheme.onPrimary),
        colors = ButtonColors(scheme.enabledButton,scheme.onPrimary, Color.Transparent, Color.Transparent)
    ){
        Text("Team options")
    }
    DropdownMenu(
        expanded = showMenu,
        onDismissRequest = { showMenu = false},
        Modifier.background(scheme.primary).border(1.dp,scheme.outline,RoundedCornerShape(6.dp))
    ){
        DropdownMenuItem(
            text = { Text("Turn on location share for members") },
            onClick = {
                showMenu = false
                serverVM.updateTeamLocSh(true)
            },
            colors = menuItemColors
        )
        com.example.jmb_bms.view.Divider(scheme.onPrimary, 1.dp)
        DropdownMenuItem(
            text = { Text("Turn off location share for members") },
            onClick = {
                showMenu = false
                serverVM.updateTeamLocSh(false)
            },
            colors = menuItemColors
        )
        com.example.jmb_bms.view.Divider(scheme.onPrimary, 1.dp)
        DropdownMenuItem(
            text = { Text("Delete team", color = if(isSystemInDarkTheme()) Color(50,0,0) else Color.Red) },
            onClick = {
                showMenu = false
                serverVM.deleteTeam()
                backHandler()
            },
            colors = menuItemColors
        )
    }
}
@Composable
fun UserListHeader(serverVM: ServerVM, backHandler: () -> Unit, onButtonClick: (() -> Unit)?)
{
    val isLeader = serverVM.pickedTeam.value.getTeamLeader()?.value?.serverId == serverVM.userProfile!!.serverId

    if(isLeader)
    {
        TeamOpsButton(serverVM,backHandler)
    }

    val scheme = LocalTheme.current

    Row(horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 10.dp)) {
        Text("Connected team members", fontSize = 25.sp, color = scheme.onPrimary)
        if(isLeader) {
            Icon(Icons.Default.Add, "", tint = scheme.outline ,modifier = Modifier.size(45.dp).clickable {
                if (onButtonClick != null) {
                    onButtonClick()
                }
            })
        }
    }
    com.example.jmb_bms.view.Divider(scheme.onPrimary, 1.dp)
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeamRefreshRateRow(profile: TeamProfile, serverVM: ServerVM)
{
    var expanded by remember { mutableStateOf(false) }

    val textFieldColors = LocalTextfieldTheme.current ?: TextFieldDefaults.colors()
    val menuItemColors = LocalMenuItemsTheme.current
    val scheme = LocalTheme.current

    Row(verticalAlignment = Alignment.CenterVertically) {
        Text("Location share refresh rate: ", fontSize = 20.sp, color = scheme.onPrimary)
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = {expanded = !expanded}
        ){
            TextField(
                modifier = Modifier.menuAnchor().border(1.dp,scheme.onPrimary).fillMaxWidth(),
                readOnly = true,
                value = profile.sharingLocDelay.menuString,
                onValueChange = {},
                label = { Text("Refresh rate") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                colors = textFieldColors,
            )
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = {expanded = false},
                Modifier.background(scheme.primary).border(1.dp,scheme.outline, RoundedCornerShape(6.dp))
            ){
                serverVM.refreshValues.forEach{
                    DropdownMenuItem(
                        text = { Text(it.menuString) },
                        onClick = {
                            profile.sharingLocDelay = it
                            expanded = false
                            serverVM.changeTeamLocShDelay(it)
                        },
                        contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding,
                        colors = menuItemColors
                    )
                    com.example.jmb_bms.view.Divider(scheme.onPrimary, 0.2.dp)
                }
            }
        }
    }
}
@Composable
fun ConnectionShareSwitch(serverVM: ServerVM)
{

    val profile by serverVM.pickedTeam.value.pair.first.observeAsState()
    val isLeader = profile?.teamLead == serverVM.userProfile!!.serverId

    if(isLeader)
    {
        CustomSwitch(profile?.thisClientSharingLoc ?: false,"Share location as team"){
            if(it)
            {
                serverVM.startSharingLocAsTeam(profile!!.sharingLocDelay)
            } else
            {
                serverVM.stopSharingLocAsTeam()
            }
        }
    }
    if(profile?.thisClientSharingLoc == true)
    {
        TeamRefreshRateRow(profile!!,serverVM)
    }
    com.example.jmb_bms.view.Divider(LocalTheme.current.onPrimary, 1.dp)
}

@Composable
fun AllDataScreen(serverVM: ServerVM, paddingValues: PaddingValues, backHandler: () -> Unit, plusButton: () -> Unit)
{
    val pair by serverVM.pickedTeam

    Column(Modifier.padding(paddingValues).background(LocalTheme.current.primary)) {
        TeamInfoRow(pair,{},null) //TODO in {} will go showing location on map if team is sharing location
        com.example.jmb_bms.view.Divider(LocalTheme.current.onPrimary, 1.dp)
        //TODO add location sharing switch which will be visible only to leader and will turn off location sharing for leader if turned on
        ConnectionShareSwitch(serverVM)
        UserListHeader(serverVM,backHandler,plusButton)

        PrintTeamMembers(pair,serverVM)
    }
}

@Composable
fun TeamDataScreen(serverVM: ServerVM, screen: _Screens, paddingValues: PaddingValues, backHandler: () -> Unit, plusButton: () -> Unit)
{
    when(screen)
    {
        _Screens.NORMAL -> AllDataScreen(serverVM, paddingValues, backHandler, plusButton)
        _Screens.EDIT -> EditingModeScreen(serverVM, paddingValues)
        _Screens.ADDING_USERS -> AddingUsersScreen(serverVM, paddingValues)
    }
}


@Composable
fun TeamDetailScreen(currLoc: LiveLocationFromLoc, currTime: LiveTime, serverVM: ServerVM, backHandler: () -> Unit)
{
    val pickedTeam by serverVM.pickedTeam
    var screen by remember{ mutableStateOf(_Screens.NORMAL) }
    val connectionState by serverVM.connectionState.observeAsState()
    val profile by pickedTeam.pair.first.observeAsState()

    val scheme = LocalTheme.current

    if(profile!!._id == "" || serverVM.userProfile == null)
    {
        Column(
            modifier = Modifier.fillMaxSize().background(scheme.onPrimary),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            CircularProgressIndicator(color = scheme.loading)
        }
    }
    else {
        val text = if (profile!!.teamLead != serverVM.userProfile!!.serverId) null else {
            when (screen) {
                _Screens.NORMAL -> "Update team"
                _Screens.EDIT -> "Edit team information"
                _Screens.ADDING_USERS -> "Add users to team"
            }
        }
        if (connectionState != ConnectionState.CONNECTED) {
            backHandler()
        }
        Scaffold(
            topBar = { MenuTop1(currTime, currLoc) },
            bottomBar = {
                BottomBar1(
                    rButtonText = text,
                    rButtonIcon = null,
                    rButtonStateColor = ButtonColors(scheme.enabledButton, scheme.onPrimary, Color.Gray, Color.White),
                    backButtonLogic = {

                        when (screen) {
                            _Screens.NORMAL -> backHandler()
                            _Screens.EDIT -> {
                                serverVM.teamEditingVM.reset()
                                screen = _Screens.NORMAL
                            }

                            _Screens.ADDING_USERS -> {
                                serverVM.addingUsersVM.reset()
                                screen = _Screens.NORMAL
                            }
                        }
                    }
                ) {
                    when (screen) {
                        _Screens.NORMAL -> screen = _Screens.EDIT
                        _Screens.EDIT -> {
                            serverVM.teamEditingVM.sendUpDatedValuesToServer()
                            screen = _Screens.NORMAL
                        }

                        _Screens.ADDING_USERS -> {
                            serverVM.addingUsersVM.addUsers()
                            screen = _Screens.NORMAL
                        }
                    }
                }
            }
        ) { padding ->
            TeamDataScreen(serverVM, screen, padding, backHandler) {
                serverVM.addingUsersVM.prepare()
                screen = _Screens.ADDING_USERS
            }
        }
    }
}

@Composable
fun TeamDetailScreenWithTheme(currLoc: LiveLocationFromLoc, currTime: LiveTime, serverVM: ServerVM, backHandler: () -> Unit)
{
    TestTheme {
        TeamDetailScreen(currLoc, currTime, serverVM, backHandler)
    }
}

enum class _Screens{
    NORMAL,
    EDIT,
    ADDING_USERS
}