package com.example.jmb_bms.model.menu

import android.content.SharedPreferences
import androidx.core.content.edit

class MenuState(private val shPref: SharedPreferences, menuName: String) {

    var order : IntArray
        private set

    private var orderString: String
    private val shKey = menuName + "OrderSH"
    private val defShVal = "1-2-3-4-5-6-7"

    private fun getIntFromStr() : IntArray = orderString.split("-")
        .mapNotNull { it.toIntOrNull() }.toIntArray()

    private fun getStrFromInt() : String = order.joinToString("-")

    init
    {
        orderString = shPref.getString(shKey,defShVal).toString()
        order = getIntFromStr()
    }

    private fun rotate(oldIndex: Int, newIndex: Int, left:  Boolean)
    {
        if(left) {
            for (counter in oldIndex until newIndex) {
                order[counter] = order[counter + 1]
            }
        } else {
            for( counter in oldIndex downTo newIndex)
            {
                if(counter != 0 || counter != newIndex)order[counter] = order[counter-1]
            }
        }
    }

    private fun updateArray(oldIndex: Int,newIndex: Int)
    {
        val value = order[oldIndex]

        rotate(oldIndex,newIndex, oldIndex <= newIndex)

        order[newIndex] = value
    }

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