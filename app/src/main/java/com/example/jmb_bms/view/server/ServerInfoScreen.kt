package com.example.jmb_bms.view.server


import android.content.Context
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.sp
import com.example.jmb_bms.model.OpenableMenuItem
import com.example.jmb_bms.viewModel.LiveLocationFromPhone
import com.example.jmb_bms.viewModel.LiveTime
import com.example.jmb_bms.viewModel.server.ServerInfoVM
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.edit
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.jmb_bms.connectionService.ConnectionState
import com.example.jmb_bms.model.icons.SymbolCreationVMHelper
import com.example.jmb_bms.model.utils.MyColorPalette
import com.example.jmb_bms.ui.theme.*
import com.example.jmb_bms.view.MenuTop2
import com.example.jmb_bms.viewModel.server.ServerVM


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyDropdownMenu(menuLabel: String, vm: SymbolCreationVMHelper, items: List<OpenableMenuItem>, state: MutableState<OpenableMenuItem>, onCLick: (vm : SymbolCreationVMHelper, item: OpenableMenuItem ) -> Unit) {

    var expanded by remember {mutableStateOf(false)}
    //var value by remember {state}
    var value by state
    val context = LocalContext.current

    val scheme = LocalTheme.current
    val textFieldColors = LocalTextfieldTheme.current ?: ExposedDropdownMenuDefaults.textFieldColors()
    val menuItemColors = LocalMenuItemsTheme.current

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = {expanded = !expanded}
    ){
        TextField(
            modifier = Modifier
                .menuAnchor()
                .clip(RoundedCornerShape(16.dp))
                .border(1.dp,scheme.outlineVariant)
                .fillMaxWidth(),
            readOnly = true,
            value = value.text,
            onValueChange = {},
            label = { Text(menuLabel) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            colors = textFieldColors,
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = {expanded = false},
            modifier = Modifier.background(scheme.primary).border(1.dp,scheme.outline,RoundedCornerShape(6.dp))
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
                    contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding,
                    colors = menuItemColors
                )
                com.example.jmb_bms.view.Divider(scheme.onPrimary,0.2.dp)
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

    val textFieldColors = LocalTextfieldTheme.current ?: TextFieldDefaults.colors()

    val borderColor = if(isSystemInDarkTheme()) darkCianColor else Color.Transparent

    TextField(
        value = ipv4 ?: "", //ipv4 ?: "",
        onValueChange = { serverInfoVM.updateIpAddress(it) },
        label = { Text("IP address or domain name", fontSize = 25.sp)},
        modifier = Modifier.fillMaxWidth().border(0.5.dp, borderColor),
        colors = textFieldColors
    )
    TextField(
        value = port ?: "",
        onValueChange = { serverInfoVM.updatePort(it)},
        label = { Text("Port", fontSize = 25.sp)},
        keyboardOptions = KeyboardOptions( keyboardType = KeyboardType.Decimal),
        modifier = Modifier.fillMaxWidth().border(0.5.dp, borderColor),
        colors = textFieldColors
    )
    TextField(
        value = userName ?: "",
        onValueChange = { serverInfoVM.updateUserName(it)},
        label = { Text("User name", fontSize = 25.sp)},
        modifier = Modifier.fillMaxWidth().border(0.5.dp, borderColor),
        colors = textFieldColors
    )
}

@Composable
fun IconCreationCascade(symbolCreationVMHelper: SymbolCreationVMHelper)//serverInfoVM: ServerInfoVM)
{
    val chosenItems by symbolCreationVMHelper.selectedOptStack.collectAsState()
    val imageBitMap by symbolCreationVMHelper.bitMap.observeAsState()

    val scheme = LocalTheme.current

    symbolCreationVMHelper.listStack.forEachIndexed{ index, _ ->

        val label = if(index == 0) "Icon Picker" else chosenItems[index - 1].value.text + "'s Submenu"
        MyDropdownMenu(label, symbolCreationVMHelper ,symbolCreationVMHelper.listStack[index],chosenItems[index]){vm, item -> vm.selectOption(index,item) }
    }
    if(imageBitMap != null)
    {
        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ){
            Text("Icon prewiew:", fontSize = 25.sp, modifier = Modifier.padding(top = 5.dp), color = scheme.onPrimary)
            Box(
                modifier = Modifier.width(200.dp).height(150.dp)
                    .padding(top = 10.dp)
                    .border(width = 2.dp, color = scheme.onPrimary, shape = RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ){
                Image(
                    bitmap = imageBitMap!!,
                    contentDescription = "User Icon",
                    modifier = Modifier.size(175.dp)
                )
            }
        }
    }
}


@Composable
fun InputInfoScreen(serverInfoVM: ServerInfoVM, padding: PaddingValues, scheme: MyColorPalette)
{
    val errorMsg by serverInfoVM.connectionErrorMsg.observeAsState()

    Column( modifier = Modifier.padding(padding).fillMaxHeight().verticalScroll(rememberScrollState()).background(scheme.primary)) {

        if(errorMsg != null &&  errorMsg != "") {

            Text(errorMsg!!, color = Color.Red, fontSize = 15.sp)
        }
        InputFields(serverInfoVM)
        IconCreationCascade(serverInfoVM.symbolCreationVMHelper)
    }
}

@Composable
fun ServerInfoInputScreen(currTime: LiveTime, currLoc: LiveLocationFromPhone, serverInfoVM: ServerInfoVM, backHandler: () -> Unit, getToNextScreen: () -> Unit)
{
    val buttonsEnabled by serverInfoVM.everyThingEntered.observeAsState()
    val correct by serverInfoVM.everyThingCorrect.observeAsState()
    val loading by serverInfoVM.loading.observeAsState(true )
    val context = LocalContext.current
    val connectionState by serverInfoVM.connectionState.observeAsState()

    val scheme = LocalTheme.current

    if(connectionState == ConnectionState.CONNECTED)
    {
        serverInfoVM.resetState()
        getToNextScreen()
    }


    Scaffold(
        topBar = { MenuTop2(currTime,currLoc,backHandler){} },
        bottomBar = {
            Row(modifier = Modifier.fillMaxWidth().background(scheme.primary)) {
                Button(
                    enabled = (buttonsEnabled ?: false) && (correct ?: false),
                    modifier = Modifier.fillMaxWidth(),
                    onClick = {serverInfoVM.connect()},
                    colors = ButtonColors(scheme.secondary, scheme.onSecondary , scheme.disabledButton, scheme.wrappingBox),
                    content = {
                        Text("Connect to server")
                    },
                    border = BorderStroke(1.dp,if(isSystemInDarkTheme()) darkCianColor else Color.Transparent)
                )}
                    },
        modifier = Modifier.background(scheme.primary)
    ){padding ->

        if(loading)
        {
            Column(
                modifier = Modifier.fillMaxSize().background(scheme.primary),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                CircularProgressIndicator(color = scheme.loading)
            }
        } else {
            InputInfoScreen(serverInfoVM, padding,scheme)
        }
    }
}

@Composable
fun ServerInfoInputScreenWithTheme(currTime: LiveTime, currLoc: LiveLocationFromPhone, serverInfoVM: ServerInfoVM, backHandler: () -> Unit, getToNextScreen: () -> Unit)
{
    TestTheme {
        ServerInfoInputScreen(currTime, currLoc, serverInfoVM, backHandler, getToNextScreen)
    }
}


@Composable
fun ServerService(currTime: LiveTime, currLoc: LiveLocationFromPhone, /*serverInfoVM: ServerInfoVM, serverVM: ServerVM,*/ backHandler: () -> Unit)
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
            val a: ServerInfoVM = viewModel(factory = ServerInfoVM.create(LocalContext.current.applicationContext))
            ServerInfoInputScreenWithTheme(currTime, currLoc, a, backHandler){
                navController.navigate("Connected"){
                    popUpTo("Entering_Info"){
                        inclusive = true
                    }
                }
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
            val a: ServerVM = viewModel(factory = ServerVM.create(LocalContext.current.applicationContext,null)) //ServerVM(LocalContext.current.applicationContext)
            ServerScreen(currLoc, currTime, a, navController , backHandler){
                navController.navigate("Entering_Info"){
                    popUpTo("Connected"){
                        inclusive = true
                    }
                }
                shPref.edit {
                    putBoolean("Normal_Screen",false)
                }
            }
            BackHandler {
                backHandler()
            }
        }

        composable("TeamDetail/{_id}")
        {
            val _id = it.arguments?.getString("_id")


            val a: ServerVM = viewModel(factory = ServerVM.create(LocalContext.current.applicationContext,_id))
            TeamDetailScreenWithTheme(currLoc,currTime,a){
                navController.navigate("Connected")
            }
        }
    }
}