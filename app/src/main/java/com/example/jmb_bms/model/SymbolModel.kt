package com.example.jmb_bms.model

import com.example.jmb_bms.model.icons.Symbol

interface SymbolModel {

    fun symbolIsValid(): Boolean

    var symbolString: String

    val symbol : Symbol

    var menuIdsString: String

    fun everyThingEntered(): Boolean

}