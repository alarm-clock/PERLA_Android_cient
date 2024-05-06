/**
 * @file: MenuItem.kt
 * @author: Jozef Michal Bukas <xbukas00@stud.fit.vutbr.cz,jozefmbukas@gmail.com>
 * Description: File containing MenuItem interface
 */
package com.example.jmb_bms.model.menu

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * Interface for one element in menu that defines attribute which implementing class must implement
 */
interface MenuItem {
    val name: String
    val icon: ImageVector
    val hardCodeID: Int
    val onAction: () -> Unit ?
    val ComposableRow: @Composable (longestString: Int) -> Unit ?
    var lastElement: Boolean
}