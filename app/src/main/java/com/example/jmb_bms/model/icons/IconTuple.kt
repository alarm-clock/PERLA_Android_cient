/**
 * @file: IconTuple.kt
 * @author: Jozef Michal Bukas <xbukas00@stud.fit.vutbr.cz,jozefmbukas@gmail.com>
 * Description: File containing IconTuple class
 */
package com.example.jmb_bms.model.icons

/**
 * Data class that holds icon code and corresponding name that will be showed in menus
 * @param iconName [IconMenuName] [String] that is icons name
 * @param iconCode [Icon] [String] that is icons code
 */
data class IconTuple(@IconMenuName val iconName: String, @Icon val iconCode: String)
