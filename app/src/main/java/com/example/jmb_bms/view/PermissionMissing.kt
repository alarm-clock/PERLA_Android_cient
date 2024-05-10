/**
 * @file: PermissionMissing.kt
 * @author: Jozef Michal Bukas <xbukas00@stud.fit.vutbr.cz,jozefmbukas@gmail.com>
 * Description: File containing permission missing func
 */
package com.example.jmb_bms.view

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.sp

@Composable
fun permissionMissing()
{
    Text("Does not have all necessary permissions to run!",color = Color.Red, fontSize = 30.sp )
}