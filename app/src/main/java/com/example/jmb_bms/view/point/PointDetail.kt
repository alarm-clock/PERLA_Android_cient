/**
 * @file PointDetail.kt
 * @author Jozef Michal Bukas <xbukas00@stud.fit.vutbr.cz,jozefmbukas@gmail.com>
 * Description: File containing composable functions for all point detail view
 */
package com.example.jmb_bms.view.point

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.QuestionMark
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.example.jmb_bms.model.utils.MimeTypes
import com.example.jmb_bms.model.utils.PointDetailFileHolder
import com.example.jmb_bms.model.utils.wgs84toMGRS
import com.example.jmb_bms.ui.theme.LocalMenuItemsTheme
import com.example.jmb_bms.ui.theme.LocalTextfieldTheme
import com.example.jmb_bms.ui.theme.LocalTheme
import com.example.jmb_bms.ui.theme.TestTheme
import com.example.jmb_bms.view.BottomBar1
import com.example.jmb_bms.view.MenuTop1
import com.example.jmb_bms.viewModel.LiveLocationFromPhone
import com.example.jmb_bms.viewModel.LiveTime
import com.example.jmb_bms.viewModel.point.PointDetailVM

@Composable
fun NameAndIconRow(vm: PointDetailVM)
{
    val pointName by vm.pointName.observeAsState()
    val pointIcon by vm.liveSymbol.observeAsState()

    val scheme = LocalTheme.current

    Box(modifier = Modifier.clip(RoundedCornerShape(16.dp)).background(scheme.darkerWrappingBox).fillMaxWidth()){
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(pointName ?: "---", fontSize = 28.sp, color = scheme.onPrimary)
                if(pointIcon != null)
                {
                    Image(pointIcon!!,"Point icon", modifier = Modifier.size(75.dp))
                } else
                {
                    Icon(Icons.Default.QuestionMark, "Icon unknown", modifier = Modifier.size(75.dp), tint = scheme.onPrimary )
                }

            }
            com.example.jmb_bms.view.Divider(scheme.onPrimary,1.dp)
            PrintLocation(vm)
        }
    }
}

@Composable
fun DescriptionRow(vm: PointDetailVM)
{
    val description by vm.pointDescription.observeAsState()

    val scheme = LocalTheme.current
    val textFieldColors = LocalTextfieldTheme.current ?: TextFieldDefaults.colors()

    TextField(
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .border(1.dp,scheme.outlineVariant, RoundedCornerShape(16.dp))
            .fillMaxWidth()
            .height(200.dp),
        enabled = false,
        textStyle = TextStyle(fontSize = 28.sp),
        value = description ?: "",
        onValueChange = {},
        label = { Text("Description:", fontSize = 20.sp) },
        colors = textFieldColors
    )
}

@Composable
fun DocumentsRow(vm: PointDetailVM, photoDetail: (holder: PointDetailFileHolder) -> Unit)
{
    val uris by vm.uris

    val scheme = LocalTheme.current

    Box(modifier = Modifier.background(scheme.darkerWrappingBox).clip(RoundedCornerShape(16.dp)).fillMaxWidth().height(220.dp))
    {
        Column(modifier = Modifier.fillMaxWidth()) {
            Text("Attached documents:", fontSize = 20.sp, color = scheme.onPrimary)
            LazyRow {
                items(uris)
                {

                    Box(modifier = Modifier.size(200.dp))
                    {
                        val _loading = remember { mutableStateOf(it.loadingState != null) }
                        val loading by _loading

                        if(loading)
                        {
                            val barVal by it.loadingState!!.observeAsState()
                            if(barVal == null){
                                _loading.value = false
                            }
                            Column {
                                CircularProgressIndicator(color = scheme.loading)
                                Text("$barVal%", fontSize = 20.sp, color = scheme.onPrimary)
                            }
                        } else
                        {
                            val painter = rememberAsyncImagePainter(model =
                            if(it.mimeType == MimeTypes.IMAGE) it.uri else vm.thumbnails.find { pair -> pair.first == it.uri  }?.second)
                            Image(
                                painter = painter,
                                contentDescription = null,
                                modifier = Modifier.padding(4.dp).size(190.dp).clickable { photoDetail(it) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PrintLocation(vm: PointDetailVM)
{
    val scheme = LocalTheme.current
    Row( modifier = Modifier.fillMaxWidth().padding(4.dp)) {
        Text("Location: ${wgs84toMGRS(vm.pointRow.location)}", fontSize = 25.sp, color = scheme.onPrimary )
    }

}

@Composable
fun OwnerAndHisState(vm: PointDetailVM)
{

}

@Composable
fun PointExtraMenu(vm: PointDetailVM, expandedVM: MutableState<Boolean>, updateHandler: () -> Unit , backHandler: () -> Unit)
{
    val expanded by expandedVM

    val menuItemColors = LocalMenuItemsTheme.current
    val scheme = LocalTheme.current

    DropdownMenu(
        expanded = expanded,
        onDismissRequest = { expandedVM.value = false},
        Modifier.background(scheme.primary).border(1.dp,scheme.outline,RoundedCornerShape(6.dp))
    ){
        DropdownMenuItem(
            text = { Text("Update point", fontSize = 20.sp)},
            onClick = {
                expandedVM.value = false
                updateHandler()
            },
            colors = menuItemColors
        )
        com.example.jmb_bms.view.Divider( scheme.onPrimary,0.2.dp)
        DropdownMenuItem(
            text = { Text("Delete point", fontSize = 20.sp, color = Color.Red) },
            onClick = {
                vm.deletePoint(backHandler)
            },
            colors = menuItemColors
        )
    }
}


@Composable
fun PointDetail(currTime: LiveTime, currLoc: LiveLocationFromPhone, vm: PointDetailVM, rButtonHandler: () -> Unit, backHandler: () -> Unit, photoDetail: (holder: PointDetailFileHolder) -> Unit)
{
    val loading by vm.loading.observeAsState()
    val canUpdate by vm.canUpdate.observeAsState()
    val deleted by vm.deleted.collectAsState()

    val scheme = LocalTheme.current

    Scaffold(
        topBar = { MenuTop1(currTime, currLoc) },
        bottomBar = {

            if(canUpdate == true && !deleted)
            {
                val expandedVM = remember { mutableStateOf(false) }
                BottomBar1(
                    "Options",
                    null,
                    ButtonColors(scheme.secondary,scheme.onSecondary,Color.Blue,Color.White),
                    backHandler,
                ){
                    expandedVM.value = true
                }
                PointExtraMenu(vm,expandedVM,rButtonHandler,backHandler)
            } else
            {
                BottomBar1(
                    null,null, ButtonColors(Color.Red, Color.Red, Color.Red, Color.Red),backHandler){}
            }

        }
    ){
        if(deleted)
        {
            Column(
                modifier = Modifier.fillMaxSize().background(scheme.primary),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Point was deleted from server :(", fontSize = 30.sp, color = scheme.onPrimary)
            }
        }
        else if(loading == true)
        {
            Column(
                modifier = Modifier.fillMaxSize().background(scheme.primary),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                CircularProgressIndicator(color = scheme.loading)
            }
        } else
        {
            Column(modifier = Modifier.padding(it).fillMaxSize().verticalScroll(rememberScrollState()).background(scheme.primary)) {
                NameAndIconRow(vm)
                DescriptionRow(vm)
                DocumentsRow(vm, photoDetail)
            }
        }
    }
}


@Composable
fun PointDetailWithTheme(currTime: LiveTime, currLoc: LiveLocationFromPhone, vm: PointDetailVM, rButtonHandler: () -> Unit, backHandler: () -> Unit, photoDetail: (holder: PointDetailFileHolder) -> Unit)
{
    TestTheme {
        PointDetail(currTime, currLoc, vm, rButtonHandler, backHandler, photoDetail)
    }
}