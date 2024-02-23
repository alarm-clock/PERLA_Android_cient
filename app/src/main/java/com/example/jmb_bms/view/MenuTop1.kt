package com.example.jmb_bms.view

import androidx.compose.foundation.background
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
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.jmb_bms.viewModel.LiveLocationFromLoc
import com.example.jmb_bms.viewModel.LiveTime

@Composable
fun TimeAndLoc(currTimeVM: LiveTime, currLocVM: LiveLocationFromLoc, fontSizeUp: TextUnit, fontSizeDown: TextUnit)
{
    val currTime by currTimeVM.currentTime.observeAsState("")
    val currLoc by currLocVM.currLocation.observeAsState("")
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
        horizontalArrangement = Arrangement.Center
    ) {
        Text(currTime, fontSize = fontSizeUp)
    }
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
        horizontalArrangement = Arrangement.Center
    ) {
        Text(currLoc ?: "Loading...", fontSize = fontSizeDown)
    }

}

@Composable
fun MenuTop1(currTimeVM: LiveTime, currLocVM: LiveLocationFromLoc)
{



    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                TimeAndLoc(currTimeVM,currLocVM,45.sp,30.sp)
            }
        }
        Divider(Color.Black,2.dp)
    }
}

@Composable
fun SmallDivider ( color : Color, thicknessDp: Dp)
{
    Box(Modifier.height(thicknessDp).background(color))
}

@Composable
fun TimeAndLocTest( fontSizeUp: TextUnit, fontSizeDown: TextUnit)
{
    Row(
        modifier = Modifier.padding(horizontal = 0.dp),
        horizontalArrangement = Arrangement.Center
    ) {
        Text("20:45:45+1:00", fontSize = fontSizeUp)
    }
    Row(
        modifier = Modifier.padding(horizontal = 0.dp),
        horizontalArrangement = Arrangement.Center
    ) {
        Text("33 UXQ 45698 78945" ?: "Loading...", fontSize = fontSizeDown)
    }
}

@Preview
@Composable
fun MenuTopTest()
{
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Row(
            modifier = Modifier.padding(horizontal = 0.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                TimeAndLocTest(40.sp,30.sp)
            }
        }
        //SmallDivider(Color.Black,2.dp)
    }

}