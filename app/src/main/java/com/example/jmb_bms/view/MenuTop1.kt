package com.example.jmb_bms.view

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.jmb_bms.viewModel.LiveLocationFromLoc
import com.example.jmb_bms.viewModel.LiveTime


@Composable
fun MenuTop1(currTimeVM: LiveTime, currLocVM: LiveLocationFromLoc,backButtonLogic: () -> Unit)
{

    val currTime by currTimeVM.currentTime.observeAsState("")
    val currLoc by currLocVM.currLocation.observeAsState("")

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(currTime, fontSize = 45.sp)
                }
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(currLoc ?: "Loading...", fontSize = 30.sp)
                }
            }
        }
        Divider(Color.Black,2.dp)
    }
}

@Preview
@Composable
fun MenuTopTest()
{
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.ChevronLeft,
                contentDescription = "Go back",
                modifier = Modifier.scale(scaleY = 1.5f, scaleX = 1.0f).clickable { TODO("It must check if it goes back within add-on or it goes to locus") }.size(51.dp),

                )
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 1.dp),
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text("20:45:45+1:00", fontSize = 45.sp)
                }
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 1.dp),
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text("33 UXQ 45698 78945" ?: "Loading...", fontSize = 30.sp)
                }
            }
        }
        Divider(Color.Black,2.dp)
    }

}