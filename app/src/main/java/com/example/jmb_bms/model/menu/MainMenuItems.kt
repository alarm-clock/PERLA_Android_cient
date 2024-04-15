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
        MainMenuItem("Settings", Icons.Filled.Settings,3, {}),
        MainMenuItem("Formatted Messages", Icons.Filled.Textsms,4, {}),
        MainMenuItem("Map Drawing", Icons.TwoTone.Map,5, {}),
        MainMenuItem("Points Management", Icons.TwoTone.Cloud,6, {
            val intent = Intent(context,PointActivity::class.java)
            context.startActivity(intent)
        }),
        MainMenuItem("Files",Icons.TwoTone.FileCopy,7, {} ),
        MainMenuItem("Clear shPrfs",Icons.Filled.Settings,8,{
            val intent = Intent(context, ClearSharedPrefDebug::class.java)
            context.startActivity(intent)
        }),
        MainMenuItem("E", Icons.Filled.Settings, 9,{
            val intent = Intent(context, E::class.java)
            context.startActivity(intent)}),
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

    private fun addDividerFlagForLastElement() { listOfItems[listOfItems.lastIndex].lastElement = true }

    private fun removeDividerFlagForLastElement() { listOfItems[listOfItems.lastIndex].lastElement = false }

    override fun changeLocationOfElement(oldIndex: Int, newIndex: Int)
    {
        if(oldIndex == listOfItems.lastIndex || newIndex == listOfItems.lastIndex) removeDividerFlagForLastElement()

        val element = listOfItems.removeAt(oldIndex)
        listOfItems.add(newIndex,element)

        if ( oldIndex == listOfItems.lastIndex || newIndex == listOfItems.lastIndex) addDividerFlagForLastElement()
    }
}