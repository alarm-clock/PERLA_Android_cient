package com.example.jmb_bms.view.chat

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.jmb_bms.ui.theme.LocalTheme
import com.example.jmb_bms.ui.theme.TestTheme
import com.example.jmb_bms.view.BottomBar1
import com.example.jmb_bms.view.MenuTop3
import com.example.jmb_bms.viewModel.LiveLocationFromPhone
import com.example.jmb_bms.viewModel.LiveTime
import com.example.jmb_bms.viewModel.chat.ChatRoomDetailVM
import com.example.jmb_bms.viewModel.chat.ChatRoomUsersVM


@Composable
fun PrintAllRoomUsers(vm: ChatRoomDetailVM)
{
    val users by vm.livePickedRoomDetail.chatUsersProfiles.observeAsState()
    LazyColumn {
        items(users ?: listOf()){
            Row {
                val profile by it.observeAsState()
                Image(profile!!.symbol!!.imageBitmap!!, "")
                Spacer(Modifier.size(20.dp))
                Text(profile?.userName ?: "")
            }
        }
    }
}

@Composable
fun PrintAllOtherUsers(vm: ChatRoomUsersVM)
{
    val users by vm.liveChatUsersMng.otherUsers.observeAsState()
    LazyColumn {

        items(users ?: listOf()){
            Row(
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                var checked by remember { mutableStateOf(false) }
                val profile by it.observeAsState()
                Row {
                    Image(profile!!.symbol!!.imageBitmap!!, "")
                    Spacer(Modifier.size(20.dp))
                    Text(profile?.userName ?: "")
                }
                Checkbox(
                    checked = checked,
                    onCheckedChange = { chck ->
                        checked = chck
                        if(checked)
                        {
                            vm.pickedUsers.add(profile!!.serverId)
                        } else
                        {
                            vm.pickedUsers.remove(profile!!.serverId)
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun ChatInfoUpdate(currLoc: LiveLocationFromPhone, currTime: LiveTime, vm: ChatRoomUsersVM, backHandler: () -> Unit)
{
    TestTheme {
        val theme = LocalTheme.current
        Scaffold(
            topBar = {
                MenuTop3(currTime,currLoc,vm.liveServiceState.connectionState)
            },
            bottomBar = {
                BottomBar1(
                    "Edit", null, ButtonColors(theme.secondary,theme.onSecondary,theme.secondary,theme.onSecondary),backHandler
                ){
                    vm.sendUpdate()
                    backHandler()
                }
            },
            modifier = Modifier.statusBarsPadding().navigationBarsPadding().imePadding()
        ){padding ->
            Column(
                modifier = Modifier.padding(padding).fillMaxSize()
            ) {
                PrintAllOtherUsers(vm)
            }
        }
    }
}

@Composable
fun ChatRoomInfo(currLoc: LiveLocationFromPhone, currTime: LiveTime, vm: ChatRoomDetailVM, backHandler: () -> Unit)
{
    TestTheme {
        val room by vm.livePickedRoomDetail.pickedRoom.collectAsState()
        val theme = LocalTheme.current
        Scaffold(
            topBar = {
                MenuTop3(currTime,currLoc,vm.serviceState.connectionState)
            },
            bottomBar = {
                BottomBar1(
                    null, null, ButtonColors(theme.secondary,theme.onSecondary,theme.secondary,theme.onSecondary),backHandler
                ){}
            },
            modifier = Modifier.statusBarsPadding().navigationBarsPadding().imePadding()
        ){padding ->
            Column(
                modifier = Modifier.padding(padding).fillMaxSize()
            ) {
                if(room != null)
                {
                    val info by room!!.info.collectAsState()
                    TextField(
                        value =  info.name,
                        enabled = false,
                        label = { Text("Room's name") },
                        onValueChange = {},
                        modifier = Modifier.fillMaxWidth()
                    )
                    PrintAllRoomUsers(vm)
                }
            }
        }
    }
}
