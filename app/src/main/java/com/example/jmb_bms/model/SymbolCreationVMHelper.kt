package com.example.jmb_bms.model

import android.content.Context
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.graphics.ImageBitmap
import androidx.lifecycle.MutableLiveData
import com.example.jmb_bms.model.icons.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.withContext
import kotlin.coroutines.CoroutineContext

class SymbolCreationVMHelper(val model: SymbolModel,val everyThingEntered: MutableLiveData<Boolean> ,val scopeLaunch: (context: CoroutineContext, code: suspend () -> Unit) -> Unit) {


    private val menuListsHelper = MenuListsHelper()
    private val iconSize = (450).toString()

    val level1 = listOf(
        OpenableMenuItem("P","Space", BattleDimension.SPACE,null),
        OpenableMenuItem("A","AIR", BattleDimension.AIR,null),
        OpenableMenuItem("G","Ground", BattleDimension.GROUND,null),
        OpenableMenuItem("S","Sea Surface", BattleDimension.SEA_SURFACE,null),
        OpenableMenuItem("U","Sea Subsurface", BattleDimension.SEA_SUBSURFACE,null),
        OpenableMenuItem("F", "SOF", BattleDimension.SOF,null),
        OpenableMenuItem("X","Other", BattleDimension.OTHER,null)
    )

    val listStack = mutableListOf(level1)
    val selectedOptStack = MutableStateFlow(listOf(mutableStateOf(level1[0]))) //mutableStateOf(listOf(mutableStateOf( level1[0])))
    val bitMap = MutableLiveData<ImageBitmap?>(null) //mutableStateOf<ImageBitmap?>(null)

    private fun editMenuString()
    {
        var str = ""
        selectedOptStack.value.forEach {
            println(it.value.id)
            str += (if(str == "") it.value.id else "|" + it.value.id)
        }
        model.menuIdsString = str
    }

    fun finishCreatingSymbol(iconCode2525: IconCode2525, context: Context)
    {
        scopeLaunch(Dispatchers.IO){

            val dimension = selectedOptStack.value.first().value.topLevelMod2525 ?: return@scopeLaunch

            model.symbol.apply {
                this.cScheme = CodingScheme.WAR_FIGHT
                this.status = Status.PRESENT
                this.affiliation = Affiliation.FRIEND
                this.dimension = dimension as BattleDimension
                this.iconCode = iconCode2525
            }

            val res = model.symbol.createIcon(context,iconSize)
            bitMap.postValue(res)
            if(res != null)
            {
                editMenuString()
                model.symbolString = model.symbol.getSymbolCode()
                everyThingEntered.postValue(model.everyThingEntered())
            } else {
                println(bitMap.value)
            }
        }
    }

    private fun prepareMenuFromTheString()
    {
        if(model.menuIdsString.isEmpty()) return

        val strList = model.menuIdsString.split('|').toMutableList()


        scopeLaunch(Dispatchers.Main){
            strList.forEachIndexed { index, it ->
                selectedOptStack.value[index].value = listStack[index].first{ listElement -> listElement.id == it  }
                selectOption(index, selectedOptStack.value[index].value)
            }
        }
    }
    fun selectOption( levelIn: Int, item: OpenableMenuItem)
    {
        val a = selectedOptStack.value.toMutableList()
        if(levelIn < listStack.size - 1)
        {
            while (levelIn < listStack.size - 1)
            {
                listStack.removeLast()
                a.removeLast()
            }
        }
        val newList = menuListsHelper.getList(item.id,levelIn) ?: return
        listStack.add(newList)
        a.add( mutableStateOf(newList[0]))
        selectedOptStack.value = a.toList()
    }

}