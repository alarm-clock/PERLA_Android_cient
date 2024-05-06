/**
 * @file: ServerEditingIconModel.kt
 * @author: Jozef Michal Bukas <xbukas00@stud.fit.vutbr.cz,jozefmbukas@gmail.com>
 * Description: File containing ServerEditingIconModel class
 */
package com.example.jmb_bms.model

import android.content.Context
import com.example.jmb_bms.model.icons.Symbol
import com.example.jmb_bms.model.icons.SymbolModel

/**
 * Class that holds data for icon creation
 */
class ServerEditingIconModel(ctx: Context): SymbolModel {

    override var symbolString: String = ""
        set(value) {
            field = value
            //symbol = Symbol(value,ctx)
        }
    override var symbol: Symbol = Symbol(ctx)
    override var menuIdsString: String = ""   //this will be always empty
    override fun everyThingEntered(): Boolean = true
    override fun symbolIsValid() = (symbol.imageBitmap != null)
}