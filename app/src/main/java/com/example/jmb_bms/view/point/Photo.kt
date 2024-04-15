package com.example.jmb_bms.view.point

import android.net.Uri
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.example.jmb_bms.ui.theme.LocalTheme
import com.example.jmb_bms.ui.theme.TestTheme
import com.example.jmb_bms.view.BottomBar1
import com.example.jmb_bms.view.MenuTop1
import com.example.jmb_bms.viewModel.LiveLocationFromLoc
import com.example.jmb_bms.viewModel.LiveTime


@Composable
fun PhotoDetail(currTime: LiveTime, currLoc: LiveLocationFromLoc, uri: Uri, backHandler: () -> Unit)
{
    TestTheme {
        val scheme = LocalTheme.current
        Scaffold(
            topBar = { MenuTop1(currTime, currLoc) },
            bottomBar = {
                BottomBar1(
                    null,null, ButtonColors(Color.Red, Color.Red, Color.Red, Color.Red),backHandler){}
            }
        ){
            Column(modifier = Modifier.padding(it).background(scheme.primary)) {
                val painter = rememberAsyncImagePainter(model = uri)
                Image(
                    painter = painter,
                    contentDescription = null,
                    modifier = Modifier.padding(4.dp).fillMaxSize()
                )
            }
        }
    }
}
