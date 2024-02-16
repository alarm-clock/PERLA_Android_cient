package com.example.jmb_bms.view

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.jmb_bms.model.MenuItem

data class MainMenuItem(override val name: String, override val icon: ImageVector,
                        override val hardCodeID: Int, override val onAction: () -> Unit ?,
                        override var lastElement: Boolean = false) : MenuItem
{

    override val ComposableRow: @Composable (longestString: Int) -> Unit?
        get() = {longestString ->
            Icon(imageVector = icon, contentDescription = null, modifier = Modifier.size(64.dp))
            Spacer(modifier = Modifier.width(90.dp))
            Text(name, fontSize = 35.sp, modifier = Modifier.width((longestString * 16).dp))
        }
}