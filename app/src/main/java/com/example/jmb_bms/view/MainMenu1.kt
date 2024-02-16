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
import androidx.compose.ui.hapticfeedback.HapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.min
import androidx.compose.ui.unit.sp
import com.example.jmb_bms.model.MenuItem
import com.example.jmb_bms.model.MenuItems
import com.example.jmb_bms.viewModel.LiveLocationFromLoc
import com.example.jmb_bms.viewModel.LiveTime
import org.burnoutcrew.reorderable.*


@Composable
fun RowData( longestString: Int,name: String, icon: ImageVector)
{
    Icon(imageVector = icon, contentDescription = null, modifier = Modifier.size(64.dp))
    Spacer(modifier = Modifier.width(100.dp))
    Text(name, fontSize = 35.sp, modifier = Modifier.width((longestString * 16).dp))
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun Item(menuItem: MenuItem, longestString: Int, clickable: Boolean = true)
{
    Row(
        modifier = if(clickable) Modifier.fillMaxWidth().padding(horizontal = 8.dp).heightIn(min = 100.dp).clickable { menuItem.onAction }
            else Modifier.fillMaxWidth().padding(horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        ) {
            menuItem.ComposableRow(longestString)
        }
}

@Composable
fun Divider ( color : Color, thicknessDp: Dp)
{
    Box(Modifier.fillMaxWidth().height(thicknessDp).background(color))
}

@Composable
fun ClasisMenu(menuItems: MenuItems, padding: PaddingValues, longestString: Int)
{
    LazyColumn(modifier = Modifier.padding(padding)) {
        itemsIndexed(menuItems.items) { index: Int, menuItem: MenuItem ->
            Item(menuItem, longestString)

            if (index < menuItems.items.lastIndex) {
                Divider(Color.Black, 1.dp)
            }
        }
    }
}

@Composable
fun ReorderableMenu(menuItems: MenuItems, padding: PaddingValues, longestString: Int)
{
    val data = remember { mutableStateOf(menuItems.items) }
    val hapticFeedback = LocalHapticFeedback.current

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
        modifier = Modifier.padding(padding).reorderable(state).detectReorderAfterLongPress(state)) {
        items(data.value,{it.hardCodeID}) {menuItem: MenuItem ->

            ReorderableItem(state, key = menuItem.hardCodeID){ isDragging ->
                val elevation = animateDpAsState(if (isDragging) 16.dp else 0.dp)
                val color = if(isDragging) MaterialTheme.colorScheme.surfaceBright else MaterialTheme.colorScheme.surface
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


@Composable
fun ScrollableMenu(menuItems: MenuItems, currTime: LiveTime, currLoc: LiveLocationFromLoc, backButtonLogic: () -> Unit)
{
    val longestString = menuItems.longestString
    var enableReord by remember { mutableStateOf(false)}

    Box( modifier = Modifier.fillMaxSize().background(Color.Gray))
    {
        Scaffold(
            bottomBar = {BottomBar1( if(enableReord) "Finish" else "Manage Tiles",null,
                if(enableReord) ButtonColors(Color.Cyan, Color.White, Color.Gray, Color.Gray)
                else ButtonColors(Color.Blue, Color.White, Color.Gray, Color.Gray),
                backButtonLogic){ enableReord = !enableReord} },
            topBar = {
                MenuTop1(currTime,currLoc,backButtonLogic)
            }
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


@Composable
fun mainMenu( currTime : LiveTime, currLoc: LiveLocationFromLoc, menuItems: MenuItems, backButtonLogic: () -> Unit)
{
    ScrollableMenu(menuItems,currTime,currLoc, backButtonLogic)
}

//For preview purposes only
