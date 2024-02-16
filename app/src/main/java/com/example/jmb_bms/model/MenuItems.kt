package com.example.jmb_bms.model

interface MenuItems {

    val items : List<MenuItem>
    val menuState: MenuState
    val longestString: Int
    fun changeLocationOfElement(oldIndex: Int, newIndex: Int)
}