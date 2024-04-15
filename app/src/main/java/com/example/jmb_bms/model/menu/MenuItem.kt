package com.example.jmb_bms.model.menu

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector

interface MenuItem {
    val name: String
    val icon: ImageVector
    val hardCodeID: Int
    val onAction: () -> Unit ?
    val ComposableRow: @Composable (longestString: Int) -> Unit ?
    var lastElement: Boolean
}