package com.example.jmb_bms.model

import android.content.Context
import com.example.jmb_bms.model.icons.Symbol

class ServerEditingIconModel(ctx: Context): SymbolModel {

    override var symbolString: String = ""
    override val symbol: Symbol = Symbol(ctx)
    override var menuIdsString: String = ""   //this will be always empty
    override fun everyThingEntered(): Boolean = true

    override fun symbolIsValid() = (symbol.imageBitmap != null)
}