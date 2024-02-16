package com.example.jmb_bms.view

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.twotone.Map
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Composable
fun BottomBar1(rButtonText: String?, rButtonIcon: ImageVector?, rButtonStateColor: ButtonColors,
               backButtonLogic: () -> Unit, onClicked: () -> Unit )
{
    Column {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceEvenly,
            modifier = Modifier.width(IntrinsicSize.Min).padding(horizontal = 1.dp).height(IntrinsicSize.Max)
        ){
            Button(
                shape = RoundedCornerShape(
                    topStart = 16.dp,
                    bottomStart = 0.dp,
                    topEnd = 0.dp,
                    bottomEnd = 0.dp
                ),
                modifier = Modifier.weight(1f).fillMaxHeight(),
                colors = ButtonColors(Color.Blue,Color.White,Color.Gray,Color.Gray),
                onClick = backButtonLogic){
                Column {
                    Row (verticalAlignment = Alignment.CenterVertically){
                        Icon( Icons.AutoMirrored.Default.ArrowBack , "Go Back")
                        Text("Back")
                    }
                }
            }
            Button(
                modifier = Modifier.weight(0.4f).fillMaxHeight(),
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
                    bottomEnd = 0.dp
                ),
                colors = rButtonStateColor,
                onClick = onClicked
            ){
                if(rButtonText != null)
                    Text(rButtonText)
                else if( rButtonIcon != null)
                    Icon(rButtonIcon,"menu")
            }
        }
    }
}

@Preview
@Composable
fun BottomBartes()
{
    Column {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceEvenly,
            modifier = Modifier.width(IntrinsicSize.Min).padding(horizontal = 1.dp).height(IntrinsicSize.Max)
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