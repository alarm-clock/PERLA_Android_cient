package com.example.jmb_bms.view


import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.jmb_bms.model.OpenableMenuItem
import com.example.jmb_bms.viewModel.LiveLocationFromLoc
import com.example.jmb_bms.viewModel.LiveTime
import com.example.jmb_bms.viewModel.ServerInfoVM
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue

@Composable
fun FirstLevelMenus()
{

}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyDropdownMenu(vm: ServerInfoVM, items: List<OpenableMenuItem> , state: MutableState<OpenableMenuItem> , onCLick: (vm : ServerInfoVM, item: OpenableMenuItem ) -> Unit) {

    var expanded by remember {mutableStateOf(false)}
    //var value by remember {state}
    var value by state

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = {expanded = !expanded}
    ){
        TextField(
            modifier = Modifier.menuAnchor().fillMaxWidth(),
            readOnly = true,
            value = value.text,
            onValueChange = {},
            label = { Text("Menu") },
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
                        it.onClick?.let { it1 -> it1(vm) }
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


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ServerInfoInputScreen(currTime: LiveTime, currLoc: LiveLocationFromLoc, serverInfoVM: ServerInfoVM)
{
    val buttonsEnabled by serverInfoVM.everyThingEntered.observeAsState()
    val chosenItems by serverInfoVM.selectedOptStack

    Scaffold(
        topBar = { MenuTop2(currTime,currLoc,{}){} }
    ){padding ->

        Column( modifier = Modifier.padding(padding)) {
            InputFields(serverInfoVM)

            serverInfoVM.listStack.forEachIndexed{ index, _ ->
                MyDropdownMenu(serverInfoVM,serverInfoVM.listStack[index],chosenItems[index]){vm, item -> vm.selectOption(index,item) }
            }

            Button(
                enabled = buttonsEnabled ?: false,
                modifier = Modifier.fillMaxWidth(),
                onClick = {},
                colors = ButtonColors(Color.Blue, Color.White, Color.Gray, Color.Black),
                content = {
                    Text("Connect to server")
                }
            )
        }
    }
}