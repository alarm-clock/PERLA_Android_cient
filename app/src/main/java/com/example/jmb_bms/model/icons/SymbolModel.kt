/**
 * @file: SymbolModel.kt
 * @author: Jozef Michal Bukas <xbukas00@stud.fit.vutbr.cz,jozefmbukas@gmail.com>
 * Description: File containing SymbolModel interface
 */
package com.example.jmb_bms.model.icons

import com.example.jmb_bms.model.icons.Symbol

/**
 * Interface that defines how should model for picking symbol look like and what necessary methods it must implement
 */
interface SymbolModel {

    /**
     * Method that check if symbol is valid
     * @return True if symbol is valid else false
     */
    fun symbolIsValid(): Boolean

    var symbolString: String

    var symbol : Symbol

    var menuIdsString: String

    /**
     * If menu requires that some things must be entered to proceed further and one of those things is symbol then
     * implement this method, else leave it empty.
     */
    fun everyThingEntered(): Boolean

}