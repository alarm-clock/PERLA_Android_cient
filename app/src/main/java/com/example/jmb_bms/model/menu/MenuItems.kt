package com.example.jmb_bms.model.menu

interface MenuItems {

    val items : List<MenuItem>
    val menuState: MenuState
    val longestString: Int
    fun changeLocationOfElement(oldIndex: Int, newIndex: Int)
}