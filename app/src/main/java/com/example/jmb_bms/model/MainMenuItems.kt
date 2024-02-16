package com.example.jmb_bms.model

import android.content.SharedPreferences
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChatBubble
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Textsms
import androidx.compose.material.icons.twotone.Cloud
import androidx.compose.material.icons.twotone.FileCopy
import androidx.compose.material.icons.twotone.Map
import com.example.jmb_bms.view.MainMenuItem

class MainMenuItems(shPref: SharedPreferences) : MenuItems {

    private var listOfItems = mutableListOf(
        MainMenuItem("Chat", Icons.Filled.ChatBubble,1, {}),
        MainMenuItem("Team", Icons.Filled.Group,2, {}),
        MainMenuItem("Settings", Icons.Filled.Settings,3, {}),
        MainMenuItem("Formatted Messages", Icons.Filled.Textsms,4, {}),
        MainMenuItem("Map Drawing", Icons.TwoTone.Map,5, {}),
        MainMenuItem("Points Management", Icons.TwoTone.Cloud,6, {}),
        MainMenuItem("Files",Icons.TwoTone.FileCopy,7, {} )
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