package com.example.jmb_bms.model

import android.content.Context
import com.example.jmb_bms.model.icons.IconTuple
import com.example.jmb_bms.model.icons.SymbolCreationVMHelper
import com.example.jmb_bms.model.icons.TopLevelMods2525

data class OpenableMenuItem(val id: String,
                            val text: String,
                            val topLevelMod2525: TopLevelMods2525?,
                            val iconTuple: IconTuple?,
                            val onClick : ((vm: SymbolCreationVMHelper, context: Context) -> Unit)? = null)
