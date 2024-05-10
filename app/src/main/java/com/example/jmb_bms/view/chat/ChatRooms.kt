/**
 * @file ChatRooms.kt
 * @author Jozef Michal Bukas <xbukas00@stud.fit.vutbr.cz,jozefmbukas@gmail.com>
 * Description: File containing composable functions for all chat rooms view. This file also contains whole navigation
 * component for chat feature
 */
package com.example.jmb_bms.view.chat

import androidx.compose.animation.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowLeft
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.jmb_bms.connectionService.ConnectionState
import com.example.jmb_bms.model.ChatMessage
import com.example.jmb_bms.model.database.chat.ChatRow
import com.example.jmb_bms.ui.theme.LocalTextfieldTheme
import com.example.jmb_bms.ui.theme.LocalTheme
import com.example.jmb_bms.ui.theme.TestTheme
import com.example.jmb_bms.ui.theme.darkCianColor
import com.example.jmb_bms.view.MenuTop4
import com.example.jmb_bms.viewModel.LiveLocationFromPhone
import com.example.jmb_bms.viewModel.LiveTime
import com.example.jmb_bms.viewModel.chat.AllChatsVM
import com.example.jmb_bms.viewModel.chat.ChatRoomDetailVM
import com.example.jmb_bms.viewModel.chat.ChatRoomUsersVM
import com.example.jmb_bms.viewModel.chat.CreateChatRoomVM
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

@Composable
fun MessageBox(message: ChatMessage)
{
    val scheme = LocalTheme.current
    if(message.id == -1L)
    {
        Text("This is start of this chat room :)", fontSize = 25.sp, color = scheme.onPrimary)
    } else
    {
        Box{
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(message.userName, fontSize = 10.sp, color = scheme.onPrimary)

                    if(message.userSymbolImage?.imageBitmap != null)
                    {
                        Image(message.userSymbolImage!!.imageBitmap!!,"",Modifier.size(15.dp))
                    }
                }
                Box(Modifier.clip(RoundedCornerShape(10.dp)).background(scheme.wrappingBox).fillMaxWidth())
                {
                    Text(message.text, fontSize = 25.sp, color = scheme.onPrimary)
                }
                Spacer(Modifier.size(3.dp))
            }
        }
    }
}

@Composable
fun ShowMessages(messages: MutableStateFlow<List<ChatMessage>>?,modifier: Modifier,onTop: ()->Unit)
{
    val scheme = LocalTheme.current
    if(messages == null)
    {
        Box(modifier = modifier, contentAlignment = Alignment.Center)
        {
            Text("Connect to server to chat", fontSize = 30.sp, color = scheme.onPrimary)
        }
    } else
    {
        val list by messages.collectAsState()
        val scrollState = rememberLazyListState()
        val firstPopulation = remember { mutableStateOf(true) }
        val cScope = rememberCoroutineScope()

        LazyColumn(modifier, scrollState) {
            item {
                LaunchedEffect( scrollState.canScrollBackward ){
                    if(!scrollState.canScrollBackward){
                        onTop()
                    }
                }
            }
            items(list, key = { it.id}){
                MessageBox(it)
            }
        }
        LaunchedEffect(list){
            if(firstPopulation.value && list.isNotEmpty())
            {
                cScope.launch {
                    scrollState.animateScrollToItem(index = list.size - 1)
                }
                firstPopulation.value = false
            }
        }
    }
}

@Composable
fun ChatInput(vm: AllChatsVM)
{
    val _msg = remember { MutableLiveData("") }
    val message by _msg.observeAsState()
    val pickedRoom by vm.liveChatRooms.pickedRoom.collectAsState()
    val connectionState by vm.liveServiceState.connectionState.observeAsState()

    val scheme = LocalTheme.current
    val textFieldColors = LocalTextfieldTheme.current ?: TextFieldDefaults.colors()

    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp))
            .border(1.dp,if(isSystemInDarkTheme()) darkCianColor else Color.Transparent,RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp))
            .background(scheme.disabledButton)
            .fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        TextField(
            enabled = connectionState == ConnectionState.CONNECTED,
            modifier = Modifier.clip(RoundedCornerShape(10.dp)).padding(5.dp).weight(1.0f),
            value = message ?: "",
            onValueChange = {
                _msg.value = it
            },
            label = {
                if(connectionState != ConnectionState.CONNECTED)
                {
                  Text("Connect to server to message", fontSize = 15.sp)
                } else if(pickedRoom !=  null)
                {
                    val info by pickedRoom!!.info.collectAsState()
                    Text("Message to ${info.name}", fontSize = 20.sp)
                } else {
                    Text("No room is picked...", fontSize = 20.sp)
                }
            },
            colors = textFieldColors
        )
        Button(
            enabled = _msg.value != null && _msg.value != "" && connectionState == ConnectionState.CONNECTED,
            onClick = {
                vm.sendMessage(_msg.value ?: "")
                _msg.value = ""
            },
            modifier = Modifier.weight(0.2f),
            colors = ButtonColors(scheme.secondary,scheme.onSecondary,scheme.disabledButton,scheme.onSecondary)
        ) {
            Icon(Icons.AutoMirrored.Default.Send,"", modifier = Modifier.weight(0.2f).height(20.dp))
        }
    }
}

@Composable
fun AllChatsColumn(vm: AllChatsVM, pickedRoomInfo: MutableStateFlow<ChatRow>?, navHostController: NavController)
{
    val chats by vm.liveChatRooms.chatRooms.collectAsState()
    val connectionState by vm.liveServiceState.connectionState.observeAsState()

    val scheme = LocalTheme.current

    Box(Modifier
        .clip(RoundedCornerShape(topEnd = 8.dp, bottomEnd = 8.dp))
        .background(scheme.darkerWrappingBox)
        .border(1.dp, scheme.outline, RoundedCornerShape(topEnd = 8.dp, bottomEnd = 8.dp))
        .fillMaxHeight()
    ) {
        LazyColumn {
            items(chats){
                val row by it.info.collectAsState()
                var enabled by remember { mutableStateOf(true) }

                if(pickedRoomInfo != null)
                {
                    val info by pickedRoomInfo.collectAsState()
                    enabled = info.id != row.id
                }

                Button(
                    enabled = enabled,
                    onClick = {
                        vm.liveChatRooms.pickRoom(row.id)
                        enabled = false
                    },
                    colors = ButtonColors(
                        scheme.pickOneBtnFromManyEn,
                        scheme.pickOneBtnFromManyInsideEn,
                        scheme.pickOneBtnFromManyDis,
                        scheme.pickOneBtnFromManyInsideDis
                    ),
                    modifier = Modifier
                        .width(220.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .border(1.dp,if(isSystemInDarkTheme()) scheme.onPrimary else Color.Transparent)
                        .padding(3.dp)
                ){
                    Text(row.name, fontSize = 24.sp)
                }
            }
            item {
                Button(
                    enabled = connectionState == ConnectionState.CONNECTED,
                    onClick = {
                        navHostController.navigate("Creation")
                    },
                    colors = ButtonColors(scheme.pickOneBtnFromManyEn, scheme.pickOneBtnFromManyInsideEn, scheme.disabledButton, scheme.wrappingBox),
                    modifier = Modifier
                        .width(220.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .border(1.dp,if(isSystemInDarkTheme()) scheme.onPrimary else Color.Transparent)
                        .padding(3.dp)
                ){

                    Icon(Icons.Default.Add,"", modifier = Modifier.size(25.dp))
                }
            }
        }
    }
}


@Composable
fun MessagesScreen(vm: AllChatsVM, paddingValues: PaddingValues, navHostController: NavController)
{
    val chatRoom by vm.liveChatRooms.pickedRoom.collectAsState()
    val isSideMenuVisible = remember { mutableStateOf(false) }

    val conf = LocalConfiguration.current
    val density = LocalDensity.current

    val screenHeightdp = conf.screenHeightDp.dp
    val offset = screenHeightdp + 100.dp

    val scheme = LocalTheme.current

    Box(
        modifier = Modifier.fillMaxSize().imePadding().padding(paddingValues).background(scheme.primary).pointerInput(Unit){//.imePadding()
            detectDragGestures { change, dragAmount ->
                if(dragAmount.x > 0)
                {
                    isSideMenuVisible.value = true
                } else if(dragAmount.x < 0)
                {
                    isSideMenuVisible.value = false
                }
            }
        }
    ){
        Box(modifier = Modifier.fillMaxSize())
        {
            Column(modifier = Modifier.fillMaxSize()) {
                ShowMessages(chatRoom?.messages,Modifier.weight(1f)){vm.fetchMessages()}

                AnimatedVisibility(
                    visible = !isSideMenuVisible.value,
                    enter = slideInVertically(initialOffsetY = { with(density){offset.roundToPx()} }),
                    exit = slideOutVertically(targetOffsetY = { with(density){offset.roundToPx()} }),
                ){
                    ChatInput(vm)
                }
            }
        }
        AnimatedVisibility(
            visible = isSideMenuVisible.value ,
            enter = slideInHorizontally(initialOffsetX = {-620}),
            exit = slideOutHorizontally(targetOffsetX = {-620})
        ) {
            AllChatsColumn(vm,chatRoom?.info,navHostController)
        }
    }
}

@Composable
fun ChatRoomOptButton(vm: AllChatsVM, expanded: MutableState<Boolean>,navHostController: NavController)
{
    val exp by expanded
    DropdownMenu(
        expanded = exp,
        onDismissRequest = {
            expanded.value = false
        }
    ){
        if(vm.userIsOwner())
        {
            DropdownMenuItem(
                text = { Text("Delete Chat Room", fontSize = 15.sp) },
                trailingIcon = { Icon(Icons.Default.Delete ,"") },
                onClick = {
                    vm.deleteRoom()
                    expanded.value = false
                }
            )
            DropdownMenuItem(
                text = { Text("Add users") },
                onClick = {
                    navHostController.navigate("Update/${vm.liveChatRooms.pickedRoom.value?.info?.value?.id}/add")
                }
            )
            DropdownMenuItem(
                text = { Text("Remove users") },
                onClick = {
                    navHostController.navigate("Update/${vm.liveChatRooms.pickedRoom.value?.info?.value?.id}/remove")
                }
            )

        } else {
            DropdownMenuItem(
                text = { Text("Leave room") },
                trailingIcon = {Icon(Icons.AutoMirrored.Default.ArrowLeft,"")},
                onClick = {
                    vm.leaveRoom()
                    expanded.value = false
                }
            )
        }
        DropdownMenuItem(
            text = { Text("Room detail") },
            onClick = {
                navHostController.navigate("Detail/${vm.liveChatRooms.pickedRoom.value?.info?.value?.id}")
            }
        )
    }
}



@Composable
fun ChatRooms(currLoc: LiveLocationFromPhone, currTime: LiveTime, vm: AllChatsVM, navHostController: NavController, backHandler: () -> Unit)
{
    TestTheme {
        Scaffold(
            topBar = {
                val exp = remember { mutableStateOf(false) }
                MenuTop4(currTime,currLoc,vm.liveServiceState.connectionState,backHandler){
                    exp.value = !exp.value
                }
                ChatRoomOptButton(vm,exp,navHostController)
            },
            modifier = Modifier.statusBarsPadding().navigationBarsPadding().imePadding()
        ){padding ->
            MessagesScreen(vm,padding,navHostController)
        }
    }
}



@Composable
fun ChatNavigation(currLoc: LiveLocationFromPhone, currTime: LiveTime, backHandler: () -> Unit)
{
    val ctx = LocalContext.current.applicationContext

    val navController = rememberNavController()

    NavHost(
        navController,
        startDestination = "Chat"
    ){
        composable("Chat"){
            val vm: AllChatsVM = viewModel(factory = AllChatsVM.create(ctx))
            ChatRooms(currLoc, currTime, vm,navController, backHandler)
        }
        composable("Creation"){
            val vm: CreateChatRoomVM = viewModel(factory = CreateChatRoomVM.create(ctx))
            CreateChatRoom(currTime,currLoc,vm,navController)
        }
        composable("Detail/{id}"){
            val id = it.arguments?.getString("id") ?: ""
            val vm: ChatRoomDetailVM = viewModel(factory = ChatRoomDetailVM.create(ctx,id))
            ChatRoomInfo(currLoc, currTime, vm){
                navController.popBackStack()
            }
        }
        composable("Update/{id}/{add}"){
            val id = it.arguments?.getString("id") ?: ""
            val add = (it.arguments?.getString("add") ?: "") == "add"
            val vm: ChatRoomUsersVM = viewModel(factory = ChatRoomUsersVM.create(ctx,id,add))
            ChatInfoUpdate(currLoc, currTime, vm){
                navController.popBackStack()
            }
        }
    }
}
