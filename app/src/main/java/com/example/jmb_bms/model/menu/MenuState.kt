/**
 * @file: MenuState.kt
 * @author: Jozef Michal Bukas <xbukas00@stud.fit.vutbr.cz,jozefmbukas@gmail.com>
 * Description: File containing MenuState class
 */
package com.example.jmb_bms.model.menu

import android.content.SharedPreferences
import androidx.core.content.edit

/**
 * Class that holds order of tiles in reorder able menu and implements method for their changing
 * @param shPref  [SharedPreferences] in which menu order is stored
 * @param menuName Name under which menu is stored in share preferences
 * @constructor Retrieves menu order from shared preferences and initializes [order] array
 */
class MenuState(private val shPref: SharedPreferences, menuName: String) {

    var order : IntArray
        private set

    private var orderString: String
    private val shKey = menuName + "OrderSH"
    private val defShVal = "1-2-3" /*-4-5-6-7*/

    /**
     * Method that converts formatted string, stored in [orderString], with integers divided by '-' into [IntArray]
     * @return [IntArray] created from formatted string
     */
    private fun getIntFromStr() : IntArray = orderString.split("-")
        .mapNotNull { it.toIntOrNull() }.toIntArray()

    /**
     * Method that converts [IntArray] into formatted string where each element is divided by '-'
     * @return [String] where each element is divided by '-'
     */
    private fun getStrFromInt() : String = order.joinToString("-")

    init
    {
        orderString = shPref.getString(shKey,defShVal).toString()
        order = getIntFromStr()
    }

    /**
     * Method that rotates elements left or right to new index in [order] array
     * @param oldIndex Index on which rated element currently is
     * @param newIndex Index where element must be moved
     * @param left If true ration will be to left else to right
     */
    private fun rotate(oldIndex: Int, newIndex: Int, left:  Boolean)
    {
        if(left) {
            for (counter in oldIndex until newIndex) {
                order[counter] = order[counter + 1]
            }
        } else {
            for( counter in oldIndex downTo newIndex)
            {
                if(counter != 0 || counter != newIndex) order[counter] = order[counter-1]
            }
        }
    }

    /**
     * Method that moves element to new location in [order] array
     * @param oldIndex Index on which rated element currently is
     * @param newIndex Index where element must be moved
     */
    private fun updateArray(oldIndex: Int,newIndex: Int)
    {
        val value = order[oldIndex]

        rotate(oldIndex,newIndex, oldIndex <= newIndex)

        order[newIndex] = value
    }

    /**
     * Method that changes index of menu item in [order] array and stores new order in [shPref]
     * @param oldIndex Index on which rated element currently is
     * @param newIndex Index where element must be moved
     * @return True if item was moved else if [oldIndex] or [newIndex] are out of bound false is returned
     */
    fun changeIndexOfMenuItem(oldIndex: Int, newIndex: Int) : Boolean
    {
        if(oldIndex < 0 || oldIndex > order.lastIndex || newIndex < 0 || newIndex > order.lastIndex)
            return false

        updateArray(oldIndex, newIndex)

        orderString = getStrFromInt()

        shPref.edit {
            putString(shKey,orderString)
            apply()
        }
        return true
    }

}