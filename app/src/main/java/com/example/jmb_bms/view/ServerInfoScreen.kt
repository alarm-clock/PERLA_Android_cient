package com.example.jmb_bms.view


import android.content.Context
import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.activity.viewModels
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.sp
import com.example.jmb_bms.model.OpenableMenuItem
import com.example.jmb_bms.viewModel.LiveLocationFromLoc
import com.example.jmb_bms.viewModel.LiveTime
import com.example.jmb_bms.viewModel.ServerInfoVM
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.edit
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.jmb_bms.connectionService.ConnectionState
import com.example.jmb_bms.viewModel.ServerVM


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyDropdownMenu(menuLabel: String, vm: ServerInfoVM, items: List<OpenableMenuItem> , state: MutableState<OpenableMenuItem> , onCLick: (vm : ServerInfoVM, item: OpenableMenuItem ) -> Unit) {

    var expanded by remember {mutableStateOf(false)}
    //var value by remember {state}
    var value by state
    val context = LocalContext.current

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = {expanded = !expanded}
    ){
        TextField(
            modifier = Modifier.menuAnchor().fillMaxWidth(),
            readOnly = true,
            value = value.text,
            onValueChange = {},
            label = { Text(menuLabel) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            colors = ExposedDropdownMenuDefaults.textFieldColors(),
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = {expanded = false}
        ){
            items.forEach{
                DropdownMenuItem(
                    text = { Text(it.text) },
                    onClick = {
                        value = it
                        expanded = false
                        onCLick(vm,it)
                        it.onClick?.let { it1 -> it1(vm, context) }
                    },
                    contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                )
            }
        }
    }
}

@Composable
fun InputFields(serverInfoVM: ServerInfoVM)
{
    val ipv4 by serverInfoVM.ipv4.observeAsState()
    val port by serverInfoVM.port.observeAsState()
    val userName by serverInfoVM.userName.observeAsState()

    TextField(
        value = ipv4 ?: "", //ipv4 ?: "",
        onValueChange = { serverInfoVM.updateIpAddress(it) },
        label = { Text("IP address or domain name", fontSize = 25.sp)},
        modifier = Modifier.fillMaxWidth()
    )
    TextField(
        value = port ?: "",
        onValueChange = { serverInfoVM.updatePort(it)},
        label = { Text("Port", fontSize = 25.sp)},
        keyboardOptions = KeyboardOptions( keyboardType = KeyboardType.Decimal),
        modifier = Modifier.fillMaxWidth()
    )
    TextField(
        value = userName ?: "",
        onValueChange = { serverInfoVM.updateUserName(it)},
        label = { Text("User name", fontSize = 25.sp)},
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
fun InputInfoScreen( serverInfoVM: ServerInfoVM, padding: PaddingValues)
{
    val chosenItems by serverInfoVM.selectedOptStack.collectAsState()
    val imageBitMap by serverInfoVM.bitMap.observeAsState()
    val errorMsg by serverInfoVM.connectionErrorMsg.observeAsState()

    Column( modifier = Modifier.padding(padding).verticalScroll(rememberScrollState())) {

        if(errorMsg != null &&  errorMsg != "") {

            Text(errorMsg!!, color = Color.Red, fontSize = 15.sp)
        }
        InputFields(serverInfoVM)

        serverInfoVM.listStack.forEachIndexed{ index, _ ->

            val label = if(index == 0) "Icon Picker" else chosenItems[index - 1].value.text + "'s Submenu"
            MyDropdownMenu(label, serverInfoVM,serverInfoVM.listStack[index],chosenItems[index]){vm, item -> vm.selectOption(index,item) }
        }
        if(imageBitMap != null)
        {
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ){
                Text("Icon prewiew:", fontSize = 25.sp, modifier = Modifier.padding(top = 5.dp))
                Box(
                    modifier = Modifier.width(200.dp).height(150.dp)
                        .padding(top = 10.dp)
                        .border(width = 2.dp, color = Color.Black, shape = RoundedCornerShape(8.dp)),
                    contentAlignment = Alignment.Center
                ){
                    Image(
                        bitmap = imageBitMap!!,
                        contentDescription = "User Icon"
                    )
                }
            }
        }
    }
}

@Composable
fun ServerInfoInputScreen(currTime: LiveTime, currLoc: LiveLocationFromLoc, serverInfoVM: ServerInfoVM, backHandler: () -> Unit, getToNextScreen: () -> Unit)
{
    val buttonsEnabled by serverInfoVM.everyThingEntered.observeAsState()
    val correct by serverInfoVM.everyThingCorrect.observeAsState()
    val loading by serverInfoVM.loading.observeAsState(true )
    val context = LocalContext.current
    val connectionState by serverInfoVM.connectionState.observeAsState()


    if(connectionState == ConnectionState.CONNECTED)
    {
        //this hopefully draws next screen
        Log.d("Server Info Screen", "Trying to get to next screen")
        serverInfoVM.resetState()
        getToNextScreen()
    }


    Scaffold(
        topBar = { MenuTop2(currTime,currLoc,backHandler){} },
        bottomBar = {
            Button(
            enabled = (buttonsEnabled ?: false) && (correct ?: false),
            modifier = Modifier.fillMaxWidth(),
            onClick = {serverInfoVM.connect()},
            colors = ButtonColors(Color.Blue, Color.White, Color.Gray, Color.Black),
            content = {
                Text("Connect to server")
            }
        )}
    ){padding ->

        if(loading)
        {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                CircularProgressIndicator()
            }
        } else {
            InputInfoScreen(serverInfoVM, padding)
        }
    }
}


@Composable
fun ServerService(currTime: LiveTime, currLoc: LiveLocationFromLoc, serverInfoVM: ServerInfoVM, serverVM: ServerVM, backHandler: () -> Unit)
{
    val navController = rememberNavController()
    val shPref = LocalContext.current.getSharedPreferences("jmb_bms_Server_Info", Context.MODE_PRIVATE)
    val connected = shPref.getBoolean("Normal_Screen",false)
    val route = if(connected) "Connected" else "Entering_Info"

    NavHost(
        navController,
        startDestination = route
    ) {
        composable("Entering_Info")
        {
            Log.d("Server Service Composable","Calling input screen")
            ServerInfoInputScreen(currTime, currLoc, serverInfoVM, backHandler){
                navController.navigate("Connected")
                shPref.edit {
                    putBoolean("Normal_Screen",true)
                }
            }
            BackHandler {
                backHandler()
            }
        }
        composable("Connected")
        {
            ServerScreen(currLoc, currTime, serverVM, backHandler){
                navController.navigate("Entering_Info")
                shPref.edit {
                    putBoolean("Normal_Screen",false)
                }
            }
            BackHandler {
                backHandler()
            }
        }
    }
}