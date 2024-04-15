package com.example.jmb_bms.model.icons

import com.example.jmb_bms.model.icons.Symbol

interface SymbolModel {

    fun symbolIsValid(): Boolean

    var symbolString: String

    var symbol : Symbol

    var menuIdsString: String

    fun everyThingEntered(): Boolean

}