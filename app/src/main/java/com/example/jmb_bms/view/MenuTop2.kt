package com.example.jmb_bms.view

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.More
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.jmb_bms.viewModel.LiveLocationFromLoc
import com.example.jmb_bms.viewModel.LiveTime

@Composable
fun MenuTop2(currTime: LiveTime, currLoc: LiveLocationFromLoc, backButtonLogic: () -> Unit, optButtonLogic: () -> Unit)
{
    Column{
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceEvenly,
            modifier = Modifier.fillMaxWidth()
        ){
            Column(Modifier.width(60.dp).clickable{ backButtonLogic()}) {
                Row(verticalAlignment = Alignment.CenterVertically){
                    Icon(Icons.AutoMirrored.Filled.ArrowBack,"Back");
                    Text("Back")
                }
            }
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                TimeAndLoc(currTime,currLoc,40.sp,30.sp)
            }
            Column( Modifier.clickable { optButtonLogic() }) {
                Icon(Icons.Filled.MoreVert,"Options",Modifier.height(50.dp))
            }
        }

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