/**
 * @file: MainMenuItems.kt
 * @author: Jozef Michal Bukas <xbukas00@stud.fit.vutbr.cz,jozefmbukas@gmail.com>
 * Description: File containing MainMenuItems class
 */
package com.example.jmb_bms.model.menu

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChatBubble
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Textsms
import androidx.compose.material.icons.twotone.Cloud
import androidx.compose.material.icons.twotone.FileCopy
import androidx.compose.material.icons.twotone.Map
import com.example.jmb_bms.activities.*
import com.example.jmb_bms.view.MainMenuItem

/**
 * Class that serves as main menu model for reordering. It defines menu elements and methods for their reordering.
 * @param shPref [SharedPreferences] in which menu order is stored
 * @param context Context used start activities when option is picked
 * @constructor Creates [menuState] from which then reorders menu like user ordered it and adds divider flag to last element
 */
class MainMenuItems(shPref: SharedPreferences, context: Context) : MenuItems {

    private var listOfItems = mutableListOf(
        MainMenuItem("Chat", Icons.Filled.ChatBubble,1, {
            val intent = Intent(context,ChatActivity::class.java)
            context.startActivity(intent)
        }),
        MainMenuItem("Team", Icons.Filled.Group,2, {
            val intent = Intent(context, TeamActivity::class.java)
            context.startActivity(intent)
        }),
        //MainMenuItem("Settings", Icons.Filled.Settings,3, {}),
        //MainMenuItem("Formatted Messages", Icons.Filled.Textsms,4, {}),
        //MainMenuItem("Map Drawing", Icons.TwoTone.Map,5, {}),
        MainMenuItem("Points Management", Icons.TwoTone.Cloud,3, {
            val intent = Intent(context,PointActivity::class.java)
            context.startActivity(intent)
        }),
        /*
        MainMenuItem("Files",Icons.TwoTone.FileCopy,7, {} ),
        MainMenuItem("Clear shPrfs",Icons.Filled.Settings,8,{
            val intent = Intent(context, ClearSharedPrefDebug::class.java)
            context.startActivity(intent)
        }),
        MainMenuItem("E", Icons.Filled.Settings, 9,{
            val intent = Intent(context, E::class.java)
            context.startActivity(intent)}),
*/
    )
    override val items : List<MainMenuItem> get() = listOfItems

    override val longestString : Int

    override val menuState = MenuState(shPref,"MainMenu")

    init{
        val order = menuState.order

        for( counter in order.indices)
        {
            val index = listOfItems.indexOfFirst { item -> item.hardCodeID == order[counter] }
            val element = listOfItems.removeAt(index)
            listOfItems.add(counter,element)
        }
        longestString = listOfItems.maxByOrNull { it.name.length }?.name?.length ?: 0
        addDividerFlagForLastElement()
    }

    /**
     * Method that adds divider flag to last element in [listOfItems]
     */
    private fun addDividerFlagForLastElement() { listOfItems[listOfItems.lastIndex].lastElement = true }

    /**
     * Method that removes divider flag to last element in [listOfItems]
     */
    private fun removeDividerFlagForLastElement() { listOfItems[listOfItems.lastIndex].lastElement = false }

    /**
     * Method that changes element location in model
     * @param oldIndex Index on which rated element currently is
     * @param newIndex Index where element must be moved
     */
    override fun changeLocationOfElement(oldIndex: Int, newIndex: Int)
    {
        if(oldIndex == listOfItems.lastIndex || newIndex == listOfItems.lastIndex) removeDividerFlagForLastElement()

        val element = listOfItems.removeAt(oldIndex)
        listOfItems.add(newIndex,element)

        if ( oldIndex == listOfItems.lastIndex || newIndex == listOfItems.lastIndex) addDividerFlagForLastElement()
    }
}