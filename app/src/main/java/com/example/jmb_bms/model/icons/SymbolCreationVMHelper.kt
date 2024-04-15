package com.example.jmb_bms.model.icons

import android.content.Context
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.graphics.ImageBitmap
import androidx.lifecycle.MutableLiveData
import com.example.jmb_bms.model.OpenableMenuItem
import com.example.jmb_bms.model.menu.MenuListsHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlin.coroutines.CoroutineContext

class SymbolCreationVMHelper {

    lateinit var model: SymbolModel
    private val menuListsHelper = MenuListsHelper()
    private val iconSize = (450).toString()
    private val complex: Boolean
    val scopeLaunch: (context: CoroutineContext, runnable: suspend () -> Unit) -> Unit
    val everyThingEntered: MutableLiveData<Boolean>?

    var imageBitmap: ImageBitmap? = null

    val level1 = listOf(
        OpenableMenuItem("P","Space", BattleDimension.SPACE,null),
        OpenableMenuItem("A","AIR", BattleDimension.AIR,null),
        OpenableMenuItem("G","Ground", BattleDimension.GROUND,null),
        OpenableMenuItem("S","Sea Surface", BattleDimension.SEA_SURFACE,null),
        OpenableMenuItem("U","Sea Subsurface", BattleDimension.SEA_SUBSURFACE,null),
        OpenableMenuItem("F", "SOF", BattleDimension.SOF,null),
        OpenableMenuItem("X","Other", BattleDimension.OTHER,null)
    )

    fun getStatusList() = Status.entries.toList()
    fun getAffiliations() = Affiliation.entries.toList()

    val listStack = mutableListOf(level1)
    val selectedOptStack = MutableStateFlow(listOf(mutableStateOf(level1[0]))) //mutableStateOf(listOf(mutableStateOf( level1[0])))
    val bitMap = MutableLiveData<ImageBitmap?>(null) //mutableStateOf<ImageBitmap?>(null)

    val selectedStatus = mutableStateOf(getStatusList()[0])
    val selectedAffiliation = mutableStateOf(getAffiliations()[0])

    constructor(everyThingEntered: MutableLiveData<Boolean>?, complex: Boolean = false, scopeLaunch: (context: CoroutineContext, runnable: suspend () -> Unit) -> Unit){
        this.everyThingEntered = everyThingEntered
        this.scopeLaunch = scopeLaunch
        this.complex = complex

        if(complex) constructorComplex()
    }
    constructor(model: SymbolModel, everyThingEntered: MutableLiveData<Boolean>?, complex: Boolean = false, scopeLaunch: (context: CoroutineContext, runnable: suspend () -> Unit) -> Unit)
    {
        this.model = model
        this.everyThingEntered = everyThingEntered
        this.scopeLaunch = scopeLaunch
        this.complex = complex

        if(complex) constructorComplex()
    }

    private fun constructorComplex()
    {
        model.symbol.affiliation = Affiliation.PENDING
        model.symbol.status = Status.PRESENT
    }

    fun pickAffiliation(newAffiliation: Affiliation, context: Context)
    {
        selectedAffiliation.value = newAffiliation

        scopeLaunch(Dispatchers.IO)
        {
            model.symbol.affiliation = newAffiliation
            if(imageBitmap != null)
            {
                createSymbol(context)
            }
        }
    }

    fun pickStatus(newStatus: Status,context: Context)
    {
        selectedStatus.value = newStatus

        scopeLaunch(Dispatchers.IO)
        {
            model.symbol.status = newStatus
            if(imageBitmap != null)
            {
                createSymbol(context)
            }
        }
    }

    private fun editMenuString()
    {
        var str = ""
        selectedOptStack.value.forEach {
            println(it.value.id)
            str += (if(str == "") it.value.id else "|" + it.value.id)
        }
        model.menuIdsString = str
    }

    private fun createSymbol(context: Context)
    {
        val res = model.symbol.createIcon(context,iconSize)
        bitMap.postValue(res)
        if(res != null)
        {
            editMenuString()
            model.symbolString = model.symbol.getSymbolCode()
            imageBitmap = res
            everyThingEntered?.postValue(model.everyThingEntered())
        } else {
            println(bitMap.value)
        }
    }

    fun finishCreatingSymbol(iconTuple: IconTuple, context: Context)
    {
        scopeLaunch(Dispatchers.IO){

            val dimension = selectedOptStack.value.first().value.topLevelMod2525 ?: return@scopeLaunch

            model.symbol.apply {
                this.cScheme = CodingScheme.WAR_FIGHT

                if(!complex)
                {
                    this.status = Status.PRESENT
                    this.affiliation = Affiliation.FRIEND
                }

                this.dimension = dimension as BattleDimension
                this.iconCode = iconTuple.iconCode
            }
            createSymbol(context)
        }
    }

    private fun setAffiliation(char: Char)
    {
        selectedAffiliation.value = getAffiliations().find { it.character == char }!!
    }

    private fun setStatus(char: Char)
    {
        selectedStatus.value = getStatusList().find { it.character == char }!!
    }

    fun prepareMenuFromTheString()
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

    fun prepareMenuFromTheString(symbolCode: String, context: Context)
    {
        prepareMenuFromTheString()

        scopeLaunch(Dispatchers.Main)
        {
            setAffiliation(symbolCode[1])
            setStatus(symbolCode[3])
            finishCreatingSymbol(selectedOptStack.value.last().value.iconTuple!!,context)
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

    fun reset()
    {
        selectOption(0,level1[0])
        listStack.removeLast()
        bitMap.postValue(null)
        model.symbolString = ""
    }

}