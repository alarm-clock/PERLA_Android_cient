/**
 * @file: MainMenuItem.kt
 * @author: Jozef Michal Bukas <xbukas00@stud.fit.vutbr.cz,jozefmbukas@gmail.com>
 * Description: File containing MainMenuItem class
 */
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
import com.example.jmb_bms.model.menu.MenuItem
import com.example.jmb_bms.ui.theme.LocalTheme

/**
 * Main menu item
 *
 * @property name Items name printed on screen
 * @property icon Icon showed next to text
 * @property hardCodeID Unique ID
 * @property onAction Closure that is invoked when item is pushed
 * @property lastElement Flag indicating that element is last
 */
data class MainMenuItem(override val name: String, override val icon: ImageVector,
                        override val hardCodeID: Int, override val onAction: () -> Unit ?,
                        override var lastElement: Boolean = false) : MenuItem
{

    /**
     * Composable row that will compose given item
     */
    override val ComposableRow: @Composable (longestString: Int) -> Unit?
        get() = {longestString ->
            Icon(imageVector = icon, contentDescription = null, modifier = Modifier.size(64.dp))
            Spacer(modifier = Modifier.width(90.dp))
            Text(name, fontSize = 35.sp, modifier = Modifier.width((longestString * 16).dp), color = LocalTheme.current.onPrimary)
        }
}