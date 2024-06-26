/**
 * @file: MenuTop3.kt
 * @author: Jozef Michal Bukas <xbukas00@stud.fit.vutbr.cz,jozefmbukas@gmail.com>
 * Description: File containing menu top and connection state row
 */
package com.example.jmb_bms.view

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Circle
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.LiveData
import com.example.jmb_bms.connectionService.ConnectionState
import com.example.jmb_bms.ui.theme.LocalTheme
import com.example.jmb_bms.viewModel.LiveLocationFromPhone
import com.example.jmb_bms.viewModel.LiveTime

/**
 * Not connected top
 *
 * @param color of the dot
 */
@Composable
fun NotConnectedTop(color: Color)
{
    Text("Disconnected", fontSize = 12.sp, color = color)
    Icon(Icons.Default.Circle, "", tint = Color.Red, modifier = Modifier.size(12.dp))
}

/**
 * Negotiating top
 *
 * @param color color of the dot
 */
@Composable
fun NegotiatingTop(color: Color)
{
    Text("Connecting...", fontSize = 12.sp, color = color)
    Icon(Icons.Default.Circle, "", tint = Color.Yellow, modifier = Modifier.size(12.dp))
}

/**
 * Connection state row
 *
 * @param connectionState Live data with connection state
 */
@Composable
fun ConnectionStateRow(connectionState: LiveData<ConnectionState>)
{
    val state by connectionState.observeAsState()
    val scheme = LocalTheme.current
    Row(
        modifier = Modifier.background(scheme.primary),
        horizontalArrangement = Arrangement.Start,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Spacer(Modifier.width(6.dp))
        Text("Connection state: ", fontSize = 12.sp, color = scheme.onPrimary)
        when(state)
        {
            ConnectionState.CONNECTED -> {
                Text("Connected", fontSize = 12.sp, color = scheme.onPrimary)
                Icon(Icons.Default.Circle,"", tint = Color.Green, modifier = Modifier.size(12.dp))
            }
            ConnectionState.ERROR -> NotConnectedTop(scheme.onPrimary)
            ConnectionState.NOT_CONNECTED -> NotConnectedTop(scheme.onPrimary)
            ConnectionState.NONE -> NotConnectedTop(scheme.onPrimary)
            ConnectionState.NEGOTIATING -> NegotiatingTop(scheme.onPrimary)
            ConnectionState.RECONNECTING -> NegotiatingTop(scheme.onPrimary)
            else -> NotConnectedTop(scheme.onPrimary)
        }
    }
}

/**
 * Menu top with connection state
 *
 * @param currTimeVM VM with time
 * @param currLocVM VM with location
 * @param connectionState Live data with connection state
 */
@Composable
fun MenuTop3(currTimeVM: LiveTime, currLocVM: LiveLocationFromPhone, connectionState: LiveData<ConnectionState>)
{

    val scheme = LocalTheme.current
    Column(
        modifier = Modifier.fillMaxWidth().background(scheme.primary)
    ){
        ConnectionStateRow(connectionState)

        Row( modifier = Modifier.fillMaxWidth())
        {
            MenuTop1(currTimeVM, currLocVM)
        }
    }
}