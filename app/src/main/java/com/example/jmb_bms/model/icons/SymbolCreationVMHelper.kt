/**
 * @file: SymbolCreationVMHelper.kt
 * @author: Jozef Michal Bukas <xbukas00@stud.fit.vutbr.cz,jozefmbukas@gmail.com>
 * Description: File containing SymbolCreationVMHelper class
 */
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

/**
 * ViewModel helper for creating symbol through menu. This class holds all live data necessary to fill view with data.
 * This class also implements methods for picking menus, storing its state and recreating it if necessary.
 */
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

    /**
     * Method that returns list of all enum entries
     * @return All entries of enum
     */
    fun getStatusList() = Status.entries.toList()

    /**
     * Method that returns list of all enum entries
     * @return All entries of enum
     */
    fun getAffiliations() = Affiliation.entries.toList()

    val listStack = mutableListOf(level1)
    val selectedOptStack = MutableStateFlow(listOf(mutableStateOf(level1[0]))) //mutableStateOf(listOf(mutableStateOf( level1[0])))
    val bitMap = MutableLiveData<ImageBitmap?>(null) //mutableStateOf<ImageBitmap?>(null)

    val selectedStatus = mutableStateOf(getStatusList()[0])
    val selectedAffiliation = mutableStateOf(getAffiliations()[0])

    /**
     * Constructor for those view-models that doesn't have model ready in time of instantiating this class but can set it
     * before it will be used.
     * @param everyThingEntered [MutableLiveData]<[Boolean]> Live flag that will update when symbol is created
     * @param complex Flag indicating if affiliation and status will be used
     * @param scopeLaunch Closure that launches coroutine that is aware of view-models lifecycle
     */
    constructor(everyThingEntered: MutableLiveData<Boolean>?, complex: Boolean = false, scopeLaunch: (context: CoroutineContext, runnable: suspend () -> Unit) -> Unit){
        this.everyThingEntered = everyThingEntered
        this.scopeLaunch = scopeLaunch
        this.complex = complex

        if(complex) constructorComplex()
    }

    /**
     * Constructor for those view-models that have model ready before instantiating this class
     * before it will be used.
     * @param model [SymbolModel] that will be used to store menu state
     * @param everyThingEntered [MutableLiveData]<[Boolean]> Live flag that will update when symbol is created
     * @param complex Flag indicating if affiliation and status will be used
     * @param scopeLaunch Closure that launches coroutine that is aware of view-models lifecycle
     */
    constructor(model: SymbolModel, everyThingEntered: MutableLiveData<Boolean>?, complex: Boolean = false, scopeLaunch: (context: CoroutineContext, runnable: suspend () -> Unit) -> Unit)
    {
        this.model = model
        this.everyThingEntered = everyThingEntered
        this.scopeLaunch = scopeLaunch
        this.complex = complex

        if(complex) constructorComplex()
    }

    /**
     * Method that also sets affiliation and status in model
     */
    private fun constructorComplex()
    {
        if(this::model.isInitialized)
        {
            model.symbol.affiliation = Affiliation.PENDING
            model.symbol.status = Status.PRESENT
        }
    }

    /**
     * Method for picking affiliation from menu and if symbol can be rendered it will be rendered
     * @param newAffiliation [Affiliation] picked in menu by user
     * @param context Context used to render symbol
     */
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

    /**
     * Method for picking status from menu and if symbol can be rendered it will be rendered
     * @param newStatus [Status] picked in menu by user
     * @param context Context used to render symbol
     */
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

    /**
     * Method that edits/creates menu string in [model] from ids in [selectedOptStack] that are divided by '|'
     */
    private fun editMenuString()
    {
        var str = ""
        selectedOptStack.value.forEach {
            //first element won't have | before it
            str += (if(str == "") it.value.id else "|" + it.value.id)
        }
        model.menuIdsString = str
    }

    /**
     * Method that creates symbol from all values stored in [model]. Also sets [everyThingEntered] attribute by calling
     * [SymbolModel.everyThingEntered]. Rendered symbol will be available in [imageBitmap] attribute.
     * @param context Context used to render symbol
     */
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
        }
    }

    /**
     * Method that is invoked when bottom level menu item is picked. This method will set all values in [Symbol]
     * and creates it. Rendered icon can be found in [imageBitmap] attribute. If error occurred [imageBitmap] will be null.
     * @param iconTuple Picked [IconTuple] from which [IconTuple.iconCode] will be used as symbol code
     * @param context Context used to render symbol
     */
    fun finishCreatingSymbol(iconTuple: IconTuple, context: Context)
    {
        scopeLaunch(Dispatchers.IO){

            val dimension = selectedOptStack.value.first().value.topLevelMod2525 ?: return@scopeLaunch

            model.symbol.apply {
                this.cScheme = CodingScheme.WAR_FIGHT

                //if it is not complex that means it is for friendly present units
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

    /**
     * Method that sets [selectedAffiliation] from [char]
     * @param char [Char] that represents symbols affiliation
     */
    private fun setAffiliation(char: Char)
    {
        selectedAffiliation.value = getAffiliations().find { it.character == char }!!
    }

    /**
     * Method that sets [selectedStatus] from [char]
     * @param char [Char] that represents symbols status
     */
    private fun setStatus(char: Char)
    {
        selectedStatus.value = getStatusList().find { it.character == char }!!
    }

    /**
     * Method that initializes menu from [SymbolModel.menuIdsString] into same state as was state when it was created.
     * Model takes menu string and from all ids it opens and sets all attributes same way as would view/user do when
     * creating symbol and that is by counting depth and invoking [selectOption] method. This will set correct values into
     * [selectedOptStack].
     */
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

    /**
     * Method that recreates menu and renders symbol from [symbolCode] and [SymbolModel.menuIdsString].
     * @param symbolCode Symbol code for symbol for which menu will be recreated
     * @param context Context for rendering symbol
     */
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

    /**
     * Method that selects option, retrieves selected sub-menu and sets it at the end of [listStack].
     * In case that previous menu was changed (for example current level is 4 and user picked something on level 1)
     * this menu removes all values from previous levels and shows new submenu.
     * @param levelIn Depth of sub-menu cascade
     * @param item Picked [OpenableMenuItem] whose sub-menu will be shown on as next sub-menu in cascade
     */
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

    /**
     * Method that resets instances state
     */
    fun reset()
    {
        selectOption(0,level1[0])
        listStack.removeLast()
        bitMap.postValue(null)
        model.symbolString = ""
    }

}