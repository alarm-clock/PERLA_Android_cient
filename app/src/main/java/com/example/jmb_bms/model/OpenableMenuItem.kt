/**
 * @file: OpenableMenuItem.kt
 * @author: Jozef Michal Bukas <xbukas00@stud.fit.vutbr.cz,jozefmbukas@gmail.com>
 * Description: File containing  OpenableMenuItem class
 */
package com.example.jmb_bms.model

import android.content.Context
import com.example.jmb_bms.model.icons.IconTuple
import com.example.jmb_bms.model.icons.SymbolCreationVMHelper
import com.example.jmb_bms.model.icons.TopLevelMods2525

/**
 * Data class that holds values for row in symbol creation
 * @param id Items id
 * @param text Text shown in view
 * @param topLevelMod2525 Enum value that modifies point
 * @param iconTuple [IconTuple] instance with icons name and code
 * @param onClick Closure that takes [SymbolCreationVMHelper] and [Context] as parameters and can be invoked any time
 */
data class OpenableMenuItem(val id: String,
                            val text: String,
                            val topLevelMod2525: TopLevelMods2525?,
                            val iconTuple: IconTuple?,
                            val onClick : ((vm: SymbolCreationVMHelper, context: Context) -> Unit)? = null)
