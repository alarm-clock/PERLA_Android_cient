/**
 * @file: BottomBar1.kt
 * @author: Jozef Michal Bukas <xbukas00@stud.fit.vutbr.cz,jozefmbukas@gmail.com>
 * Description: File containing bottom bar view
 */
package com.example.jmb_bms.view

import androidx.activity.ComponentActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.twotone.Map
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.jmb_bms.ui.theme.*
import locus.api.android.utils.LocusUtils

/**
 * Bottom bar. If you can only pass text or icon for right button. If you don't pass anything only back and map
 * buttons will be shown on the screen
 *
 * @param rButtonText
 * @param rButtonIcon
 * @param rButtonStateColor
 * @param backButtonLogic
 * @param onClicked
 */
@Composable
fun BottomBar1(rButtonText: String?, rButtonIcon: ImageVector?, rButtonStateColor: ButtonColors,
               backButtonLogic: () -> Unit, onClicked: () -> Unit )
{
    val oneIsNotNull = (rButtonText != null) || (rButtonIcon != null)
    val context = LocalContext.current
    val scheme = LocalTheme.current

    Row(
        modifier = Modifier.fillMaxWidth().background(scheme.primary)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceEvenly,
            modifier = Modifier
                .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
                .border(1.dp,if(isSystemInDarkTheme()) darkCianColor else Color.Transparent,RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp) )
                .height(IntrinsicSize.Max)
                .fillMaxWidth() //.padding(horizontal = 1.dp)
        ){
            Button(
                shape = RoundedCornerShape(
                    topStart = 16.dp,
                    bottomStart = 0.dp,
                    topEnd = 0.dp,
                    bottomEnd = 0.dp
                ),
                modifier = Modifier.weight( if(oneIsNotNull) 1f else 1.0f ).fillMaxHeight(),
                colors = ButtonColors(scheme.secondary,scheme.onSecondary,Color.Gray,Color.Gray),
                onClick = backButtonLogic){
                Column {
                    Row (verticalAlignment = Alignment.CenterVertically){
                        Icon( Icons.AutoMirrored.Default.ArrowBack , "Go Back")
                        Text("Back")
                    }
                }
            }
            Button(
                modifier = Modifier
                    .weight(if( oneIsNotNull ) 0.4f else 1.0f)
                    .fillMaxHeight(),
                colors = ButtonColors(if(isSystemInDarkTheme()) Color(20,0,20) else Purple80, scheme.onPrimary,scheme.disabledButton,scheme.disabledButton),
                shape = RoundedCornerShape(0.dp,if( oneIsNotNull ) 0.dp else 16.dp ,0.dp,0.dp),
                onClick = {
                    LocusUtils.callStartLocusMap(context)
                    (context as? ComponentActivity)?.finish()
                }
            ){
                Icon(Icons.TwoTone.Map,"To map",Modifier.fillMaxSize(), tint = scheme.onSecondary)
            }
            if(oneIsNotNull)
            {
                Button(
                    modifier = Modifier.weight(1f).fillMaxHeight(),
                    shape = RoundedCornerShape(
                        topStart = 0.dp,
                        bottomStart = 0.dp,
                        topEnd = 16.dp,
                        bottomEnd = 0.dp
                    ),
                    colors = rButtonStateColor,
                    onClick = onClicked
                ) {
                    if (rButtonText != null)
                        Text(rButtonText)
                    else if (rButtonIcon != null)
                        Icon(rButtonIcon, "menu")
                }
            }
        }
    }
}

@Preview
@Composable
fun BottomBartes()
{
    Column() {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceEvenly,
            modifier = Modifier.width(IntrinsicSize.Min).padding(horizontal = 1.dp).height(IntrinsicSize.Max).heightIn(min = 130.dp)
        ){
            Button(
                shape = RoundedCornerShape(
                        topStart = 16.dp,
                        bottomStart = 16.dp,
                        topEnd = 0.dp,
                        bottomEnd = 0.dp
                    ),
                modifier = Modifier.weight(1f).fillMaxHeight(),
                colors = ButtonColors(Color.Blue,Color.White,Color.Gray,Color.Gray),
                onClick = {}){
                Column {
                    Row (verticalAlignment = Alignment.CenterVertically){
                        Icon( Icons.AutoMirrored.Default.ArrowBack , "Go Back")
                        Text("Back")
                    }
                }
            }
            Button(
                modifier = Modifier.fillMaxHeight().weight(0.5f),
                shape = RoundedCornerShape(0,0,0,0),
                onClick = {}
            ){
                Icon(Icons.TwoTone.Map,"To map",Modifier.fillMaxSize())
            }
            Button(
                modifier = Modifier.weight(1f).fillMaxHeight(),
                shape = RoundedCornerShape(
                        topStart = 0.dp,
                        bottomStart = 0.dp,
                        topEnd = 16.dp,
                        bottomEnd = 16.dp
                    ),
                colors = ButtonColors(Color.Blue,Color.White,Color.Gray,Color.Gray),
                onClick = {}
            ){
                Text("Manage tiles")
            }
        }
    }
}