/**
 * @file AllPoints.kt
 * @author Jozef Michal Bukas <xbukas00@stud.fit.vutbr.cz,jozefmbukas@gmail.com>
 * Description: File containing composable functions for all points view
 */
package com.example.jmb_bms.view.point

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.jmb_bms.model.PointMenuRow
import com.example.jmb_bms.ui.theme.LocalCheckBoxTheme
import com.example.jmb_bms.ui.theme.LocalMenuItemsTheme
import com.example.jmb_bms.ui.theme.LocalTheme
import com.example.jmb_bms.ui.theme.TestTheme
import com.example.jmb_bms.view.BottomBar1
import com.example.jmb_bms.view.MenuTop3
import com.example.jmb_bms.viewModel.LiveLocationFromPhone
import com.example.jmb_bms.viewModel.LiveTime
import com.example.jmb_bms.viewModel.point.AllPointsVM


@Composable
fun FirstMenu(vm: AllPointsVM, paddingValues: PaddingValues)
{
    val ctx = LocalContext.current
    val scheme = LocalTheme.current

    Column(
        modifier = Modifier.padding(paddingValues).fillMaxSize().background(scheme.primary)
    ){
        Box(
            modifier = Modifier
                .weight(1f, true)
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(scheme.wrappingBox)
                .border(2.dp,if(isSystemInDarkTheme()) scheme.onPrimary else  Color.Blue, RoundedCornerShape(16.dp))
                .clickable {
                    vm.selectPoints(0, ctx)
                },
            contentAlignment = Alignment.Center
        ){
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceEvenly
            ){
                Spacer(Modifier.width(3.dp))
                Text("All Points", fontSize = 35.sp, color = scheme.onPrimary)
                Spacer(Modifier.width(15.dp))
                Icon(Icons.Default.Storage, "Storage", modifier = Modifier.size(40.dp), tint = scheme.onPrimary)
                Spacer(Modifier.width(3.dp))
            }

        }
        Box(Modifier.weight(0.1f))
        Box(
            modifier = Modifier
                .weight(1f, true)
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(scheme.wrappingBox)
                .border(2.dp,if(isSystemInDarkTheme()) scheme.onPrimary else Color.Green, RoundedCornerShape(16.dp))
                .clickable {
                    vm.selectPoints(1, ctx)
                },
            contentAlignment = Alignment.Center
        ){
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Spacer(Modifier.width(3.dp))
                Text("My Points", fontSize = 35.sp, color = scheme.onPrimary)
                Spacer(Modifier.width(15.dp))
                Icon(Icons.Default.Person, "Storage", modifier = Modifier.size(40.dp), tint = scheme.onPrimary)
                Spacer(Modifier.width(3.dp))
            }
        }
        Box(Modifier.weight(0.1f))
        Box(
            modifier = Modifier
                .weight(1f, true)
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(scheme.wrappingBox)
                .border(2.dp,if(isSystemInDarkTheme()) scheme.onPrimary else Color.Red, RoundedCornerShape(16.dp))
                .clickable {
                    vm.selectPoints(2, ctx)
                },
            contentAlignment = Alignment.Center
        ){
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Spacer(Modifier.width(3.dp))
                Text("Shared Points", fontSize = 35.sp, color = scheme.onPrimary)
                Spacer(Modifier.width(15.dp))
                Icon(Icons.Default.Cloud, "Storage", modifier = Modifier.size(40.dp), tint = scheme.onPrimary)
                Spacer(Modifier.width(3.dp))
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun PointMenuBox(data: PointMenuRow,navController: NavController , vm: AllPointsVM)
{
    val symbol by data.liveSymbol.observeAsState()
    val name by data.liveName.observeAsState()
    val visible by data.liveVisible.observeAsState()
    var expanded by remember { mutableStateOf(false) }
    val ctx = LocalContext.current
    val picking by vm.picking.collectAsState()
    var checked by remember { mutableStateOf(false) }

    val scheme = LocalTheme.current
    val menuItemColors = LocalMenuItemsTheme.current
    val checkboxColors = LocalCheckBoxTheme.current ?: CheckboxDefaults.colors()

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(scheme.darkerWrappingBox)
            .border(0.5.dp,if(isSystemInDarkTheme()) scheme.onPrimary else Color.Transparent, RoundedCornerShape(16.dp))
    ){
        Row(
            modifier = Modifier.fillMaxWidth().padding(3.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
           Row(
               verticalAlignment = Alignment.CenterVertically,
               horizontalArrangement = Arrangement.Start,
               modifier = Modifier.combinedClickable(

                   enabled = true,
                   onLongClick = {
                   expanded = true
               }){
                   if(picking)
                   {
                       checked = !checked
                       if(checked)
                       {
                           vm.pickedPointsIds.add(data.id)
                       } else
                       {
                           vm.pickedPointsIds.remove(data.id)
                       }
                   } else
                   {
                       vm.showPointDetail()
                       navController.navigate(_PointScreens.DETAIL.route + "/${data.id}/false")
                   }

               }
           ) {
               if(symbol != null) Image(symbol!!.imageBitmap!!, "Icon", modifier = Modifier.size(70.dp))
               Spacer(Modifier.width(30.dp))
               Text(name?: "", fontSize = 20.sp, modifier = Modifier.widthIn(max = 200.dp), color = scheme.onPrimary)
           }

            if(picking)
            {

                Checkbox(
                    checked = checked,
                    onCheckedChange = {
                        if(it)
                        {
                            vm.pickedPointsIds.add(data.id)
                            checked = true
                        } else
                        {
                            vm.pickedPointsIds.remove(data.id)
                            checked = false
                        }
                    },
                    colors = checkboxColors
                )
            } else
            {
                Icon(
                    if(visible == true) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                    "Visibility",
                    modifier = Modifier.clickable{
                        vm.changePointVisibility(data.id, !visible!! , ctx)
                    }.size(40.dp),
                    tint = scheme.onPrimary
                )
            }
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false},
            Modifier.background(scheme.primary).border(1.dp,scheme.outline,RoundedCornerShape(6.dp))
        ){
            DropdownMenuItem(
                text = { Text("Show on Map",fontSize = 20.sp) },
                trailingIcon = { Icon(Icons.Default.Map,"") },
                onClick = {
                    vm.centerAndOpenLocus(data.id,ctx)
                },
                colors = menuItemColors
            )
            com.example.jmb_bms.view.Divider(scheme.onPrimary,0.2.dp)
            val deleteStr = if(data.ownedByClient) "Delete Point" else "Delete Point Locally"
            DropdownMenuItem(
                text = { Text(deleteStr, fontSize = 20.sp, color = Color.Red) },
                onClick = {
                    expanded = false
                    vm.deletePoint(data.id,ctx)
                },
                trailingIcon = { Icon(Icons.Default.Delete, "", tint = Color.Red)},
                colors = menuItemColors
            )
        }
    }
}

@Composable
fun ListPoints(vm: AllPointsVM, paddingValues: PaddingValues, navController: NavController)
{
    val shownPoints by vm.shownPoints.collectAsState()

    val scheme = LocalTheme.current

    if(shownPoints == null)
    {
        Column(
            modifier = Modifier.padding(paddingValues)
        ) {
            Box {
                Text("No points to show here :(", fontSize = 35.sp)
            }
        }
    } else
    {
        Column(Modifier.padding(paddingValues).fillMaxSize().background(scheme.primary)) {
            LazyColumn{
                items(shownPoints ?: listOf()){
                    PointMenuBox(it,navController,vm)
                    Box(Modifier.size(1.dp))
                }
            }
        }
    }
}

@Composable
fun ShowMenus(vm: AllPointsVM, paddingValues: PaddingValues,navController: NavController)
{
    val shownPoints by vm.shownPoints.collectAsState()

    if(shownPoints == null)  //TODO change so that empty list will still show something
    {
        FirstMenu(vm,paddingValues)
    } else
    {
        ListPoints(vm,paddingValues,navController)
    }
}

@Composable
fun PickingDropdownMenu(vm: AllPointsVM, exp: MutableState<Boolean>)
{
    val expanded by exp
    val ctx = LocalContext.current
    val pickedList by vm.pickedListLiveCode.observeAsState()

    val deleteString = if(pickedList == 2) "Delete Multiple Points Locally" else "Delete Multiple Points"

    val scheme = LocalTheme.current
    val menuItemColors = LocalMenuItemsTheme.current

    DropdownMenu(
        expanded = expanded,
        onDismissRequest = {exp.value = false},
        Modifier.background(scheme.primary).border(1.dp,scheme.outline,RoundedCornerShape(6.dp))
    ){
        DropdownMenuItem(
            text = { Text("Make Multiple Points Visible", fontSize = 20.sp) },
            trailingIcon = { Icon(Icons.Default.Visibility, "") },
            onClick ={
                exp.value = false
                vm.markAndMakeVis(ctx)
            },
            colors = menuItemColors
        )
        com.example.jmb_bms.view.Divider(scheme.onPrimary,0.2.dp)
        DropdownMenuItem(
            text = { Text("Make Multiple Points Invisible", fontSize = 20.sp) },
            trailingIcon = { Icon(Icons.Default.VisibilityOff, "") },
            onClick ={
                exp.value = false
                vm.markAndMakeInVis(ctx)
            },
            colors = menuItemColors
        )
        com.example.jmb_bms.view.Divider(scheme.onPrimary,0.2.dp)
        DropdownMenuItem(
            text = { Text(deleteString, fontSize = 20.sp, color = Color.Red) },
            trailingIcon = { Icon(Icons.Default.Delete, "", tint = Color.Red) },
            onClick ={
                exp.value = false
                vm.markAndDelete(ctx)
            },
            colors = menuItemColors
        )
        com.example.jmb_bms.view.Divider(scheme.onPrimary,0.2.dp)
        DropdownMenuItem(
            text = { Text("Sync all points with server", fontSize = 20.sp) },
            trailingIcon = { Icon(Icons.Default.Refresh, "") },
            onClick ={
                exp.value = false
                vm.manuallySync()
            },
            colors = menuItemColors
        )
    }

}


@Composable
fun AllPoints(currTime: LiveTime, currLoc: LiveLocationFromPhone, vm: AllPointsVM, navController: NavController, backHandler: () -> Unit)
{

    val shownPoints by vm.shownPoints.collectAsState()
    val loading by vm.loading.observeAsState()

    val exp = remember { mutableStateOf(false) }
    var expanded by remember { mutableStateOf(false) }
    val picking by vm.picking.collectAsState()
    val ctx = LocalContext.current

    val scheme = LocalTheme.current

    Scaffold(
        topBar = { MenuTop3(currTime,currLoc,vm.liveConnectionState.connectionState) },
        bottomBar = {
            BottomBar1(
                if(shownPoints != null) "Pick Multiple" else null,
                null,
                ButtonColors(scheme.secondary, scheme.onSecondary , scheme.disabledButton, scheme.onPrimary),
                {
                    if(picking)
                    {
                        vm.stopPicking()
                    } else if(shownPoints == null)
                    {
                        backHandler()
                    } else
                    {
                        vm.reset()
                    }
                },
            ){
                if(picking)
                {
                    vm.finnishMarkingMultiple(ctx)
                } else
                {
                    exp.value = true
                }
            }
            PickingDropdownMenu(vm,exp)
        }
    ){padding ->

        if(loading == null || loading == true)
        {
            Column(
                modifier = Modifier.padding(padding).fillMaxSize().background(scheme.primary),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                CircularProgressIndicator(color = scheme.loading)
            }
        } else
        {
            ShowMenus(vm,padding,navController)
        }
    }
}

@Composable
fun AllPointsWithTheme(currTime: LiveTime, currLoc: LiveLocationFromPhone, vm: AllPointsVM, navController: NavController, backHandler: () -> Unit)
{
    TestTheme {
        AllPoints(currTime, currLoc, vm, navController, backHandler)
    }
}