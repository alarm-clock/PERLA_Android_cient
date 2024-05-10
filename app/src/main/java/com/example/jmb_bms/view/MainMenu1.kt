/**
 * @file: MainMenu1.kt
 * @author: Jozef Michal Bukas <xbukas00@stud.fit.vutbr.cz,jozefmbukas@gmail.com>
 * Description: File containing view for main menu
 */
package com.example.jmb_bms.view

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.jmb_bms.model.menu.MenuItem
import com.example.jmb_bms.model.menu.MenuItems
import com.example.jmb_bms.ui.theme.LocalTheme
import com.example.jmb_bms.ui.theme.TestTheme
import com.example.jmb_bms.viewModel.LiveLocationFromPhone
import com.example.jmb_bms.viewModel.LiveTime
import org.burnoutcrew.reorderable.*


@Composable
fun RowData( longestString: Int,name: String, icon: ImageVector)
{
    Icon(imageVector = icon, contentDescription = null, modifier = Modifier.size(64.dp))
    Spacer(modifier = Modifier.width(100.dp))
    Text(name, fontSize = 35.sp, modifier = Modifier.width((longestString * 16).dp), color = MaterialTheme.colorScheme.onPrimary)
}

/**
 * Menu item
 *
 * @param menuItem
 * @param longestString Length of longest string
 * @param clickable
 */
@Composable
fun Item(menuItem: MenuItem, longestString: Int, clickable: Boolean = true)
{
    val scheme = LocalTheme.current
    Row(
        modifier = if(clickable) Modifier.background(scheme.primary).fillMaxWidth().padding(horizontal = 8.dp)
            .heightIn(min = 100.dp).clickable { menuItem.onAction() }
            else Modifier.background(scheme.primary).fillMaxWidth().padding(horizontal = 8.dp).heightIn(min = 100.dp),

        verticalAlignment = Alignment.CenterVertically,
        ) {
            menuItem.ComposableRow(longestString)
        }
}

/**
 * Small line
 *
 * @param color
 * @param thicknessDp
 */
@Composable
fun Divider ( color : Color, thicknessDp: Dp)
{
    Box(Modifier.fillMaxWidth().height(thicknessDp).background(color))
}

/**
 * Classic menu
 *
 * @param menuItems
 * @param padding
 * @param longestString
 */
@Composable
fun ClasisMenu(menuItems: MenuItems, padding: PaddingValues, longestString: Int)
{
    val scheme = LocalTheme.current
    LazyColumn(modifier = Modifier.padding(padding).fillMaxSize().background(scheme.primary)) {
        itemsIndexed(menuItems.items) { index: Int, menuItem: MenuItem ->
            Item(menuItem, longestString)

            //if (index < menuItems.items.lastIndex) {
                Divider(scheme.onPrimary, 1.dp)
            //}
        }
    }
}

/**
 * Reorder-able main menu version
 *
 * @param menuItems
 * @param padding
 * @param longestString
 */
@Composable
fun ReorderableMenu(menuItems: MenuItems, padding: PaddingValues, longestString: Int)
{
    val data = remember { mutableStateOf(menuItems.items) }
    val hapticFeedback = LocalHapticFeedback.current
    val scheme = LocalTheme.current

    val state = rememberReorderableLazyListState(onMove = {old , new ->

        data.value = data.value.toMutableList().apply {
            add(new.index, removeAt(old.index))
        }
        menuItems.menuState.changeIndexOfMenuItem(old.index,new.index)
        menuItems.changeLocationOfElement(old.index,new.index)

        hapticFeedback?.performHapticFeedback(HapticFeedbackType.LongPress)
    })

    LazyColumn(
        state = state.listState,
        modifier = Modifier.padding(padding).reorderable(state).detectReorderAfterLongPress(state).fillMaxSize().background(scheme.primary)) {
        items(data.value,{it.hardCodeID}) {menuItem: MenuItem ->

            ReorderableItem(state, key = menuItem.hardCodeID){ isDragging ->
                val elevation = animateDpAsState(if (isDragging) 16.dp else 0.dp)
                val color = if(isDragging) MaterialTheme.colorScheme.surfaceBright else scheme.primary
                Column(modifier = Modifier.shadow(elevation.value).background(color)) {

                    Item(menuItem, longestString,false)
                    if( !menuItem.lastElement)
                    {
                        Divider(Color.Black,1.dp)
                    }
                }
                if( isDragging )
                {
                    hapticFeedback?.performHapticFeedback(HapticFeedbackType.LongPress)
                }
            }
        }
    }
}

/**
 * Scrollable menu
 *
 * @param menuItems
 * @param currTime
 * @param currLoc
 * @param backButtonLogic
 */
@Composable
fun ScrollableMenu(menuItems: MenuItems, currTime: LiveTime, currLoc: LiveLocationFromPhone, backButtonLogic: () -> Unit)
{
    val longestString = menuItems.longestString
    var enableReord by remember { mutableStateOf(false)}

    val scheme = LocalTheme.current

    Box( modifier = Modifier.background(scheme.primary).fillMaxSize())
    {
        Scaffold(
            bottomBar = {BottomBar1( if(enableReord) "Finish" else "Manage Tiles",null,
                if(enableReord) ButtonColors(scheme.tertiary, scheme.onSecondary, Color.Gray, Color.Gray)
                else ButtonColors(scheme.secondary, scheme.onSecondary, Color.Gray, Color.Gray),
                backButtonLogic){ enableReord = !enableReord} },
            topBar = {
                MenuTop1(currTime,currLoc)
            },
        ){ padding ->

            if(enableReord)
            {
                ReorderableMenu(menuItems, padding, longestString)
            } else
            {
                ClasisMenu(menuItems,padding,longestString)
            }
        }
    }
}

/**
 * Main menu
 *
 * @param currTime
 * @param currLoc
 * @param menuItems
 * @param backButtonLogic
 * @receiver
 */
@Composable
fun mainMenu(currTime : LiveTime, currLoc: LiveLocationFromPhone, menuItems: MenuItems, backButtonLogic: () -> Unit)
{
    TestTheme() {
        ScrollableMenu(menuItems,currTime,currLoc, backButtonLogic)
    }
}

//For preview purposes only
