package com.example.jmb_bms.view.chat

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.MutableLiveData
import androidx.navigation.NavController
import com.example.jmb_bms.connectionService.models.UserProfile
import com.example.jmb_bms.ui.theme.*
import com.example.jmb_bms.view.BottomBar1
import com.example.jmb_bms.view.MenuTop3
import com.example.jmb_bms.viewModel.LiveLocationFromPhone
import com.example.jmb_bms.viewModel.LiveTime
import com.example.jmb_bms.viewModel.chat.CreateChatRoomVM


@Composable
fun RoomNameInput(vm: CreateChatRoomVM)
{
    val name by vm.chatName.observeAsState()

    val textFieldColors = LocalTextfieldTheme.current ?: TextFieldDefaults.colors()

    TextField(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .border(0.5.dp, if(isSystemInDarkTheme()) darkCianColor else Color.Transparent,RoundedCornerShape(8.dp))
            .fillMaxWidth(),
        value = name ?: "",
        onValueChange = {
            vm.editName(it)
        },
        label = { Text("Chat room name", fontSize = 20.sp) },
        colors = textFieldColors
    )
}

@Composable
fun UserTickableRow(profile: MutableLiveData<UserProfile>, onChange: (id: String, picked: Boolean) -> Unit)
{
    val prof by profile.observeAsState()
    var picked by remember{ mutableStateOf(false) }

    val scheme = LocalTheme.current
    val checkboxColors = LocalCheckBoxTheme.current ?: CheckboxDefaults.colors()

    Box(Modifier
        .clip(RoundedCornerShape(8.dp))
        .background(color = scheme.darkerWrappingBox)
        .border(0.5.dp, if(isSystemInDarkTheme()) darkCianColor else Color.Transparent,RoundedCornerShape(8.dp))
        .fillMaxWidth()
        ){
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable {
                           picked = !picked
                           onChange(prof?.serverId ?: "",picked)

            },
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ){
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Start
            ) {
                if(prof?.symbol?.imageBitmap != null) Image(prof!!.symbol!!.imageBitmap!!,"", modifier = Modifier.size(70.dp))
                Spacer(Modifier.width(30.dp))
                Text(prof?.userName ?: "", fontSize = 20.sp, modifier = Modifier.widthIn(max = 200.dp), color = scheme.onPrimary)
            }

            Checkbox(
                checked = picked,
                onCheckedChange = {
                    picked = !picked
                    onChange(prof?.serverId ?: "", picked)
                },
                colors = checkboxColors
            )
        }
    }

}


@Composable
fun AllUsersList(vm: CreateChatRoomVM)
{
    val users by vm.liveUsers.connectedUsers.collectAsState()

    LazyColumn {
        items(users){
            UserTickableRow(it){ id, picked ->
                if(picked)
                {
                    vm.pickedUsersList.add(id)
                } else
                {
                    vm.pickedUsersList.remove(id)
                }
            }
        }
    }

}

@Composable
fun CreateChatRoom(currTime: LiveTime, currLoc: LiveLocationFromPhone, vm: CreateChatRoomVM, navHostController: NavController)
{
    TestTheme {
        val scheme = LocalTheme.current
        val canCreate by vm.canCreateRoom.collectAsState()
        Scaffold(
            topBar = { MenuTop3(currTime,currLoc,vm.liveServiceState.connectionState) },
            bottomBar = {
                BottomBar1("Create room",null, ButtonColors(scheme.secondary,scheme.onSecondary,scheme.disabledButton,scheme.onSecondary),
                { navHostController.popBackStack() }){
                    if(canCreate)
                    {
                        vm.createChatRoom()
                        navHostController.popBackStack()
                    }
            } },
            modifier = Modifier.statusBarsPadding().navigationBarsPadding()
        ){padding ->

            Column(modifier = Modifier.padding(padding).background(scheme.primary)){
                RoomNameInput(vm)
                AllUsersList(vm)
            }

        }
    }
}