/**
 * @file: MenuItems.kt
 * @author: Jozef Michal Bukas <xbukas00@stud.fit.vutbr.cz,jozefmbukas@gmail.com>
 * Description: File containing MenuItems interface
 */
package com.example.jmb_bms.model.menu

/**
 * Interface that defines attributes and method for movable menu
 */
interface MenuItems {

    val items : List<MenuItem>
    val menuState: MenuState
    val longestString: Int
    fun changeLocationOfElement(oldIndex: Int, newIndex: Int)
}