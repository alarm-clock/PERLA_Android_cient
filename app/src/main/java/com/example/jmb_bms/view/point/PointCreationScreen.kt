/**
 * @file PointCreationScreen.kt
 * @author Jozef Michal Bukas <xbukas00@stud.fit.vutbr.cz,jozefmbukas@gmail.com>
 * Description: File containing composable functions for all point creation view. This file also contains navigation component
 * for whole point feature.
 */
package com.example.jmb_bms.view.point

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.activity.result.ActivityResultRegistry
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import coil.compose.rememberAsyncImagePainter
import com.example.jmb_bms.activities.MainActivity
import com.example.jmb_bms.model.icons.SymbolCreationVMHelper
import com.example.jmb_bms.model.database.points.PointDBHelper
import com.example.jmb_bms.model.utils.MimeTypes
import com.example.jmb_bms.model.utils.MyColorPalette
import com.example.jmb_bms.model.utils.wgs84toMGRS
import com.example.jmb_bms.ui.theme.*
import com.example.jmb_bms.view.BottomBar1
import com.example.jmb_bms.view.MenuTop3
import com.example.jmb_bms.view.server.IconCreationCascade
import com.example.jmb_bms.viewModel.LiveLocationFromPhone
import com.example.jmb_bms.viewModel.LiveTime
import com.example.jmb_bms.viewModel.point.AllPointsVM
import com.example.jmb_bms.viewModel.point.PointCreationVM
import com.example.jmb_bms.viewModel.point.PointDetailVM
import locus.api.objects.extra.Location
import locus.api.objects.geoData.Point
import java.net.URLEncoder

@Composable
fun PrintLocation(vm: PointCreationVM, scheme: MyColorPalette)
{
    val ctx = LocalContext.current
    val updating by vm.updating.observeAsState()

    val textFieldColors = LocalTextfieldTheme.current ?: TextFieldDefaults.colors()

    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        TextField(
            value = wgs84toMGRS(if(updating == true) vm.row.location else{ if(updating == false) vm.point.location else Location(0.0,0.0)
            }),
            enabled = true,
            label = { Text("Location", fontSize = 20.sp) },
            textStyle = TextStyle(fontSize = 28.sp),
            readOnly = true,
            onValueChange = {},
            modifier = Modifier.clip(RoundedCornerShape(16.dp)).border(1.dp,scheme.outlineVariant, RoundedCornerShape(16.dp)).weight(0.8f),
            colors = textFieldColors
        )
        if(updating == true)
        {
            Button(
                modifier = Modifier.weight(0.2f),
                onClick = {vm.editLocation(ctx)},
                colors = ButtonColors(scheme.secondary,scheme.onSecondary,Color.Gray,Color.Gray),
            ){
                Text("Edit")
            }
        }
    }

}

@Composable
fun PointNameEntering(vm: PointCreationVM, scheme: MyColorPalette)
{
    val name by vm.pointName.observeAsState()

    val textFieldColors = LocalTextfieldTheme.current ?: TextFieldDefaults.colors()

    TextField(
        value = name ?: "",
        onValueChange = {
            vm.editName(it)
        },
        modifier = Modifier.clip(RoundedCornerShape(16.dp)).border(1.dp,scheme.outlineVariant, RoundedCornerShape(16.dp)).fillMaxWidth(),
        label = { Text("Point name", fontSize =  20.sp) },
        textStyle = TextStyle(fontSize = 28.sp),
        colors = textFieldColors
    )
}

@Composable
fun DescriptionInput(vm: PointCreationVM, scheme: MyColorPalette)
{
    val description by vm.pointDescription.observeAsState()
    val textFieldColors = LocalTextfieldTheme.current ?: TextFieldDefaults.colors()

    TextField(
        value = description ?: "",
        onValueChange ={
            vm.editDescription(it)
        },
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .border(1.dp,scheme.outlineVariant, RoundedCornerShape(16.dp))
            .fillMaxWidth()
            .height(200.dp),
        label = { Text("Point description", fontSize = 20.sp)},
        textStyle = TextStyle(fontSize = 28.sp),
        colors = textFieldColors
    )
}

@Composable
fun CreateIconRow(opened: MutableState<Boolean>,sh: SymbolCreationVMHelper, scheme: MyColorPalette)
{
    val symbol by sh.bitMap.observeAsState()

    Row(
        modifier = Modifier.fillMaxWidth().clickable { opened.value = !opened.value }.padding(top = 3.dp, end = 3.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Spacer(Modifier.width(2.dp))
        Text("Icon", fontSize = 40.sp, color = scheme.onPrimary)
        if(symbol == null)
        {
            Icon(Icons.Default.Add,"Add icon", modifier = Modifier.size(75.dp), tint = scheme.onPrimary)
        } else
        {
            Image(symbol!!,"Icon", modifier = Modifier.size(75.dp))
        }
    }
    //com.example.jmb_bms.view.Divider(Color.Black, 1.dp)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateIconStatusPicker(sh: SymbolCreationVMHelper, scheme: MyColorPalette)
{
    var expanded by remember {mutableStateOf(false)}
    var value by sh.selectedStatus
    val context = LocalContext.current

    val textFieldColors = LocalTextfieldTheme.current ?: ExposedDropdownMenuDefaults.textFieldColors()
    val menuItemColors = LocalMenuItemsTheme.current

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = {expanded = !expanded}
    ){
        TextField(
            modifier = Modifier.menuAnchor().clip(RoundedCornerShape(16.dp)).fillMaxWidth(),
            readOnly = true,
            value = value.toString(),
            onValueChange = {},
            label = { Text("Status") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            colors = textFieldColors,
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = {expanded = false},
            modifier = Modifier.background(scheme.primary).border(1.dp,scheme.outline,RoundedCornerShape(6.dp))
        ){
            sh.getStatusList().forEach{
                DropdownMenuItem(
                    text = { Text(it.toString()) },
                    onClick = {
                        value = it
                        expanded = false
                        sh.pickStatus(it,context)
                    },
                    contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding,
                    colors = menuItemColors
                )
                com.example.jmb_bms.view.Divider(scheme.onPrimary,0.2.dp)
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateIconAffiliationPicker(sh: SymbolCreationVMHelper, scheme: MyColorPalette)
{
    var expanded by remember {mutableStateOf(false)}
    var value by sh.selectedAffiliation
    val context = LocalContext.current

    val textFieldColors = LocalTextfieldTheme.current ?: ExposedDropdownMenuDefaults.textFieldColors()
    val menuItemColors = LocalMenuItemsTheme.current

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = {expanded = !expanded}
    ){
        TextField(
            modifier = Modifier.menuAnchor().clip(RoundedCornerShape(16.dp)).fillMaxWidth(),
            readOnly = true,
            value = value.toString(),
            onValueChange = {},
            label = { Text("Affiliation") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            colors = textFieldColors,
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = {expanded = false},
            modifier = Modifier.background(scheme.primary).border(1.dp,scheme.outline,RoundedCornerShape(6.dp))
        ){
            sh.getAffiliations().forEach{
                DropdownMenuItem(
                    text = { Text(it.toString()) },
                    onClick = {
                        value = it
                        expanded = false
                        sh.pickAffiliation(it,context)
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
fun CreateIconOptions(opened: MutableState<Boolean>, sh: SymbolCreationVMHelper,scheme: MyColorPalette)
{
    val open by opened

    if(open)
    {
        Column(modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState())) {
            CreateIconStatusPicker(sh,scheme)
            CreateIconAffiliationPicker(sh,scheme)
            IconCreationCascade(sh)
        }
    }

}

@Composable
fun CreateIcon(vm: PointCreationVM, scheme: MyColorPalette)
{
    val opened = remember { mutableStateOf(false) }
    val open by opened

    val modifier = if(open) Modifier
        .clip(RoundedCornerShape(16.dp))
        .background(scheme.inversePrimary)
        .border(2.dp, color = scheme.outline, shape = RoundedCornerShape(16.dp))
        .fillMaxWidth()
        .height(600.dp)
    else Modifier
        .clip(RoundedCornerShape(16.dp))
        .background(scheme.inversePrimary)
        .border(2.dp, color = scheme.outline, shape = RoundedCornerShape(16.dp))
        .fillMaxWidth()

    Box(
        modifier = modifier,
    ){
        Column {
            CreateIconRow(opened, vm.symbolCreationVMHelper, scheme)
            CreateIconOptions(opened, vm.symbolCreationVMHelper, scheme)
        }
    }
}

@Composable
fun createPhotoInput(vm: PointCreationVM, scheme: MyColorPalette)
{
    var expanded by remember { mutableStateOf(false) }

    val menuItemColors = LocalMenuItemsTheme.current

    Box(modifier = Modifier.size(200.dp).clickable { expanded = true }, contentAlignment = Alignment.Center){

        Column(
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(Icons.Default.Add, "Add Document", modifier =  Modifier.size(100.dp),scheme.onPrimary)
            Text("Add document", fontSize = 20.sp, color = scheme.onPrimary)
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false},
            modifier = Modifier.background(scheme.primary).border(1.dp,scheme.outline,RoundedCornerShape(6.dp))
        ){
            DropdownMenuItem(
                text = { Text("Add photo", fontSize = 30.sp) },
                onClick = {
                    expanded = false
                    vm.takePhotoFromCamera()
                },
                colors = menuItemColors
            )
            com.example.jmb_bms.view.Divider(scheme.onPrimary,0.2.dp)
            /*
            DropdownMenuItem(
                text = { Text("Add video", fontSize = 30.sp) },
                onClick = {
                    expanded = false
                    vm.takeVideoFromCamera()
                },
                colors = menuItemColors
            )*/
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OwnerPicker(vm: PointCreationVM, scheme: MyColorPalette)
{
    var expanded by remember { mutableStateOf(false) }
    val value by vm.owner.collectAsState()

    val menuItemColors = LocalMenuItemsTheme.current
    val textFieldColors = LocalTextfieldTheme.current ?: ExposedDropdownMenuDefaults.textFieldColors()

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded}
    ){
        TextField(
            modifier = Modifier.menuAnchor().clip(RoundedCornerShape(16.dp)).fillMaxWidth(),
            readOnly = true,
            value = value,
            onValueChange = {},
            label = { Text("Owner") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            colors = textFieldColors,
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = {expanded = false},
            modifier = Modifier.background(scheme.primary).border(1.dp,scheme.outline,RoundedCornerShape(6.dp))
        ){
            DropdownMenuItem(
                text = { Text("All") },
                onClick = {
                    expanded = false
                    vm.owner.value = "All"
                },
                colors = menuItemColors
            )
            com.example.jmb_bms.view.Divider(scheme.onPrimary,0.2.dp)
            DropdownMenuItem(
                text = { Text("Me") },
                onClick = {
                    expanded = false
                    vm.owner.value = "Me"
                },
                colors = menuItemColors
            )
        }
    }
}



@OptIn(ExperimentalFoundationApi::class)
@Composable
fun PhotoInput(vm: PointCreationVM, photoDetail: (uri: Uri) -> Unit, scheme: MyColorPalette, videoPlay: (uri: Uri) -> Unit)
{
    val photos by vm.liveUris.collectAsState()
    var expanded by remember { mutableStateOf(false) }

    Box(modifier = Modifier.background(scheme.wrappingBox).clip(RoundedCornerShape(16.dp)).fillMaxWidth().height(200.dp))
    {
        LazyRow {
            item {
                createPhotoInput(vm,scheme)
            }
            items(photos) {

                Box(modifier = Modifier.size(200.dp))
                {
                    val painter = rememberAsyncImagePainter(model = it.first)
                    Image(
                        painter = painter,
                        contentDescription = null,
                        modifier = Modifier.padding(4.dp).size(190.dp).combinedClickable(
                            onClick = {
                                Log.d("What type",it.second.toString())
                                if(it.second == MimeTypes.IMAGE) photoDetail(Uri.parse(it.first))
                                else if( it.second == MimeTypes.VIDEO ) videoPlay(Uri.parse(it.first))

                                      },
                            onLongClick = { expanded = true }
                        )  ///clickable { photoDetail(Uri.parse(it)) }
                    )
                }
                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false },
                    modifier = Modifier.background(scheme.primary).border(1.dp,scheme.outline,RoundedCornerShape(6.dp))
                ) {
                    DropdownMenuItem(
                        text = { Text("Delete file", fontSize = 20.sp, color = Color.Red) },
                        trailingIcon = { Icon(Icons.Default.Delete, "Delete Point", tint = Color.Red) },
                        onClick = {
                            expanded = false
                            vm.removeUri(it.first)
                        }
                    )
                }
            }
        }
    }
}


@Composable
fun NameAndDescription(vm: PointCreationVM,scheme: MyColorPalette)
{
    Box(modifier =  Modifier.clip(RoundedCornerShape(16.dp)).background(scheme.wrappingBox).fillMaxWidth())
    {
        Column(modifier = Modifier.padding(2.dp)) {
            PrintLocation(vm,scheme)
            PointNameEntering(vm,scheme)
            DescriptionInput(vm,scheme)
        }
    }

}
@Composable
fun PointCreationScaffold(currTime: LiveTime, currLoc: LiveLocationFromPhone, vm: PointCreationVM, navHostController: NavController? = null, fromLoc: Boolean, backHandler: () -> Unit, photoDetail: (uri: Uri) -> Unit)
{
    val bitmap by vm.symbolCreationVMHelper.bitMap.observeAsState()
    val loading by vm.loading.observeAsState()
    val updating by vm.updating.observeAsState()

    val scheme = LocalTheme.current

    Scaffold(
        topBar = { MenuTop3(currTime, currLoc, vm.liveServiceState.connectionState) },
        bottomBar = {

            var rButtonText: String? = null

            if(updating == false)
            {
                rButtonText = "CreatePoint"
            }

            BottomBar1(
                null,null, ButtonColors(Color.Red,Color.Red,Color.Red,Color.Red),{
                    if(updating == false)
                    {
                        vm.liveUris.value.forEach {
                            vm.removeUri(it.first)
                        }
                    }
                    backHandler()
                }){}
        }
    ){
        if(loading == true) {
            Column(
                modifier = Modifier.background(scheme.primary).fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                CircularProgressIndicator(color = scheme.loading) //purple in light mode
            }
        } else {
            Column(modifier = Modifier.padding(it).background(scheme.primary)/*.verticalScroll(rememberScrollState())*/) {

                Column(modifier = Modifier.weight(1f).verticalScroll(rememberScrollState())) {
                    CreateIcon(vm,scheme)
                    Box(modifier = Modifier.size(5.dp))
                    NameAndDescription(vm,scheme)
                    PhotoInput(vm,photoDetail,scheme){uri ->
                        val encodedUri = URLEncoder.encode(uri.toString(), "UTF-8")

                        //TODO check this navhostcontroller
                        navHostController?.navigate("${_PointScreens.VIDEO.route}/${encodedUri}")
                    }
                    OwnerPicker(vm,scheme)
                    if(updating == false)
                    {
                        Button(
                            modifier = Modifier.fillMaxWidth(),
                            enabled = bitmap != null,
                            onClick = {
                                vm.createPointOffline()
                                backHandler()
                            },
                            colors = ButtonColors(
                                scheme.enabledButton,
                                scheme.onPrimary,
                                scheme.disabledButton,
                                if(isSystemInDarkTheme()) darkGrey20 else Color.White
                            ),
                            content = { Text("Create point offline", fontSize = 25.sp) },
                            border = BorderStroke(1.dp,if(isSystemInDarkTheme()) scheme.onPrimary else Color.Transparent )
                        )
                    }
                }

                if(updating == true)
                {
                    Button(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = {
                            vm.updatePoint{
                                navHostController?.popBackStack()

                                while (navHostController?.popBackStack() == true){}

                                navHostController?.navigate(_PointScreens.DETAIL.route + "/${vm.row.id}/${if(fromLoc) "true" else "false"}")
                            }
                        },
                        content = { Text("Update point", fontSize = 25.sp) }
                    )
                } else
                {
                    Button(
                        modifier = Modifier.fillMaxWidth(),
                        enabled = bitmap != null,
                        onClick = {
                            vm.createPointOnline()
                            backHandler()
                        },
                        colors = ButtonColors(
                            scheme.enabledButton,
                            if(isSystemInDarkTheme()) Color.Magenta  else  Color.Green,
                            scheme.disabledButton,
                            if(isSystemInDarkTheme()) darkGrey20 else Color.Green
                            ),
                        content = { Text("Create point online", fontSize = 32.sp) },
                        border = BorderStroke(1.dp,if(isSystemInDarkTheme()) scheme.onPrimary else Color.Transparent )
                    )
                }
            }
        }
    }
}


@Composable
fun PointCreation(currTime: LiveTime, currLoc: LiveLocationFromPhone, vm: PointCreationVM, navHostController: NavHostController? = null, fromLoc: Boolean, backHandler: () -> Unit, photoDetail: (uri: Uri) -> Unit )
{
    TestTheme{
        PointCreationScaffold(currTime, currLoc, vm, navHostController, fromLoc, backHandler, photoDetail)
    }
}
@Composable
fun AllPointScreens(
    point: Point,
    currTime: LiveTime,
    currLoc: LiveLocationFromPhone,
    activityResultRegistry: ActivityResultRegistry,
    dbHelper: PointDBHelper,
    screen: _PointScreens,
    locBundle: Bundle?,
    explicit: Boolean = false,
    fromLoc: Boolean = true,
    backHandler: () -> Unit
) {
    val navController = rememberNavController()
    val ctx = LocalContext.current.applicationContext

    NavHost(
        navController,
        startDestination = screen.route
    ){
        composable(_PointScreens.CREATION.route){
            val vm: PointCreationVM = viewModel( factory = PointCreationVM.create(ctx,activityResultRegistry,dbHelper))
            vm.point = point
            PointCreation(currTime, currLoc, vm,navController, false ,backHandler){ uri ->

                val encodedUri = URLEncoder.encode(uri.toString(), "UTF-8")
                navController.navigate("${_PointScreens.PHOTO.route}/${encodedUri}")
            }
        }
        composable(_PointScreens.CREATION.route + "/{id}"){
            val id = it.arguments?.getString("id")?.toLong() ?: -1
            val vm: PointCreationVM = viewModel( factory = PointCreationVM.create(id,ctx,activityResultRegistry, dbHelper))

            PointCreation(currTime,currLoc,vm,navController,fromLoc,{navController.popBackStack()}){ uri ->

                val encodedUri = URLEncoder.encode(uri.toString(), "UTF-8")
                navController.navigate("${_PointScreens.PHOTO.route}/${encodedUri}")
            }
        }
        composable(_PointScreens.DETAIL.route){

            val vm: PointDetailVM = viewModel( factory = PointDetailVM.create(point, dbHelper, ctx))
            PointDetailWithTheme(currTime,currLoc,vm,{ navController.navigate(_PointScreens.CREATION.route + "/${vm.pointRow.id}")}, backHandler){ holder ->
                val encodedUri = URLEncoder.encode(holder.uri.toString(), "UTF-8")

                when(holder.mimeType){
                    MimeTypes.IMAGE -> navController.navigate("${_PointScreens.PHOTO.route}/${encodedUri}")
                    MimeTypes.VIDEO -> navController.navigate("${_PointScreens.VIDEO.route}/${encodedUri}")
                    else -> Unit
                }
            }
        }
        composable("${_PointScreens.DETAIL.route}/{id}/{toLoc}"){
            val id = it.arguments?.getString("id")?.toLong() ?: -1
            val toLoc = it.arguments?.getString("toLoc")?.compareTo("false") != 0 //!= "false"

            val vm: PointDetailVM = viewModel( factory = PointDetailVM.create(id,dbHelper,ctx))

            PointDetailWithTheme(currTime,currLoc,vm,
                {navController.navigate(_PointScreens.CREATION.route + "/$id")},
                { if(toLoc) backHandler()  else navController.navigate(_PointScreens.ALL.route)}
            ){ holder ->
                val encodedUri = URLEncoder.encode(holder.uri.toString(), "UTF-8")

                when(holder.mimeType){
                    MimeTypes.IMAGE -> navController.navigate("${_PointScreens.PHOTO.route}/${encodedUri}")
                    MimeTypes.VIDEO -> navController.navigate("${_PointScreens.VIDEO.route}/${encodedUri}")
                    else -> Unit
                }

            }
        }
        composable(_PointScreens.CREATION_FROM_LOC.route)
        {
            val vm: PointCreationVM = viewModel(factory = PointCreationVM.create(locBundle,ctx,activityResultRegistry, dbHelper))

            PointCreation(currTime,currLoc,vm,navController,fromLoc,
                {navController.navigate("${_PointScreens.DETAIL.route}/${vm.row.id}/${if(fromLoc) "false" else "true"}")}
            ){ uri ->
                val encodedUri = URLEncoder.encode(uri.toString(), "UTF-8")
                navController.navigate("${_PointScreens.PHOTO.route}/${encodedUri}")
            }
        }
        composable(_PointScreens.ALL.route) {

            val vm: AllPointsVM = viewModel(factory = AllPointsVM.create(ctx,dbHelper))
            val context = LocalContext.current

            AllPointsWithTheme(currTime,currLoc,vm,navController) {
                if(explicit)
                {
                    vm.reset()
                    val intent = Intent(context,MainActivity::class.java)
                    context.startActivity(intent)
                } else
                {
                    vm.reset()
                    backHandler()
                }
            }

        }
        composable("${_PointScreens.PHOTO.route}/{uri}")
        {
            val uriString = it.arguments?.getString("uri")

            if(uriString == null) navController.popBackStack()
            val uri = Uri.parse(uriString)
            PhotoDetail(currTime, currLoc, uri){
                navController.popBackStack()
            }
        }

        /*
        composable("${_PointScreens.PHOTO.route}/{uri}")
        {
            val uriString = it.arguments?.getString("uri")

            if(uriString == null) navController.popBackStack()
            val uri = Uri.parse(uriString)

            PhotoDetail(currTime,currLoc, uri, backHandler)
        }

         */
        composable("${_PointScreens.VIDEO.route}/{uri}")
        {
            val uriString = it.arguments?.getString("uri")

            if(uriString == null) navController.popBackStack()
            val uri = Uri.parse(uriString)

            VideoPlay(uri)
        }
    }
}

//TODO add video

enum class _PointScreens(val route: String){
    CREATION("Point_Creation"),
    DETAIL("Point_Detail"),
    ALL("All_points"),
    PHOTO("Photo"),
    VIDEO("Video"),
    CREATION_FROM_LOC("Creation_from_loc")
}