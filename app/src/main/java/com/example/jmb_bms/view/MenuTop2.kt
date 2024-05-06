package com.example.jmb_bms.view

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.LiveData
import com.example.jmb_bms.connectionService.ConnectionState
import com.example.jmb_bms.ui.theme.LocalTheme
import com.example.jmb_bms.viewModel.LiveLocationFromPhone
import com.example.jmb_bms.viewModel.LiveTime


@Composable
fun MenuTop4(currTime: LiveTime, currLoc: LiveLocationFromPhone, connectionState: LiveData<ConnectionState>, backButtonLogic: () -> Unit, optButtonLogic: () -> Unit)
{
    val scheme = LocalTheme.current
    Column(modifier = Modifier.fillMaxWidth().background(scheme.primary))
    {
        ConnectionStateRow(connectionState)
        Row(modifier = Modifier.fillMaxWidth()) {
            MenuTop2(currTime, currLoc, backButtonLogic, optButtonLogic)
        }
    }
}



@Composable
fun MenuTop2(currTime: LiveTime, currLoc: LiveLocationFromPhone, backButtonLogic: () -> Unit, optButtonLogic: () -> Unit)
{
    val scheme = LocalTheme.current
    Column(modifier = Modifier.background(scheme.primary)){
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth()
        ){
            Column(Modifier.weight(0.2f).clickable{
                Log.d("Menu Top 2 Back Button","Calling back button logic")
                backButtonLogic()
            }) {
                Row(verticalAlignment = Alignment.CenterVertically){
                    Icon(Icons.AutoMirrored.Filled.ArrowBack,"Back", tint = scheme.onPrimary);
                    Text("Back", color = scheme.onPrimary)
                }
            }
            Column(
                modifier = Modifier.weight(0.8f),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                TimeAndLoc(currTime,currLoc,32.sp,22.sp)
            }
            Column( Modifier.weight(0.1f).clickable { optButtonLogic() }) {
                Icon(Icons.Filled.MoreVert,"Options",Modifier.size(50.dp),scheme.onPrimary)
            }
        }
        Divider(scheme.onPrimary,1.dp)

    }
}

@Preview
@Composable
fun MenuTop2T()
{
    Column{
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceEvenly,
            modifier = Modifier.fillMaxWidth()
        ){
            Column(Modifier.width(60.dp) ) {
                Row(verticalAlignment = Alignment.CenterVertically){
                    Icon(Icons.AutoMirrored.Filled.ArrowBack,"Back");
                    Text("Back")
                }
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center) { TimeAndLocTest(40.sp,30.sp) }
            Column { Icon(Icons.Filled.MoreVert,"Options",Modifier.height(50.dp))}
        }

    }
}