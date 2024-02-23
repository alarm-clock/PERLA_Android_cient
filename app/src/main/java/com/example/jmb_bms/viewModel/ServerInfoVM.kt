package com.example.jmb_bms.viewModel

import android.content.Context
import android.content.Context.MODE_PRIVATE
import androidx.annotation.DimenRes
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.*
import com.example.jmb_bms.model.*
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


class ServerInfoVM(context: Context) : ViewModel() {

    private lateinit var model: ServerInfo
    var ipv4 = MutableLiveData<String>("")
    val port = MutableLiveData<String>("")
    val userName = MutableLiveData<String>("")
    val everyThingEntered = MutableLiveData<Boolean>(false)

    private var ipStoreJob : Job? = null
    private var portStoreJob : Job? = null
    private var nameStoreJob : Job? = null
    private val jobDelay: Long = 1000

    private val menuListsHelper = MenuListsHelper()

    val selectedOpts = mutableStateOf(listOf<OpenableMenuItem>())

    val level1 = listOf(OpenableMenuItem("P","Space",BattleDimension.SPACE,null),
        OpenableMenuItem("A","AIR",BattleDimension.AIR,null),
        OpenableMenuItem("G","Ground",BattleDimension.GROUND,null),
        OpenableMenuItem("S","Sea Surface",BattleDimension.SEA_SURFACE,null),
        OpenableMenuItem("U","Sea Subsurface",BattleDimension.SEA_SUBSURFACE,null),
        OpenableMenuItem("F", "SOF", BattleDimension.SOF,null),
        OpenableMenuItem("X","Other",BattleDimension.OTHER,null)
        )

    val listStack = mutableListOf(level1)
    val selectedOptStack = mutableStateOf(listOf(mutableStateOf( level1[0])))
    val selectedOpt = mutableStateOf(level1[0])
    init {

        viewModelScope.launch {
            model = ServerInfo( context, context.getSharedPreferences("jmb_bms_Server_Info", MODE_PRIVATE))

            ipv4.value = model.ipV4
            port.value = model.portString
            userName.value = model.userName
            everyThingEntered.value = model.everyThingEntered()
        }
    }

    private fun updateIPModel(newAddr: String)
    {
        ipStoreJob?.cancel()
        ipStoreJob = viewModelScope.launch {
            delay(jobDelay)
            model.ipV4 = newAddr
            everyThingEntered.value = model.everyThingEntered()
        }
    }
    fun updateIpAddress( newAddress: String)
    {
        ipv4.value = newAddress
        updateIPModel(newAddress)
    }

    private fun updatePortModel(newPort: String)
    {
        portStoreJob?.cancel()
        portStoreJob = viewModelScope.launch {
            delay(jobDelay)
            model.portString = newPort
            everyThingEntered.value = model.everyThingEntered()
        }
    }
    fun updatePort( newPort: String)
    {
        port.value = newPort
        updatePortModel(newPort)
    }

    private fun updateUserNameModel(newName: String)
    {
        nameStoreJob?.cancel()
        nameStoreJob = viewModelScope.launch {
            delay(jobDelay)
            model.userName = newName
            everyThingEntered.value = model.everyThingEntered()
        }
    }
    fun updateUserName( newName: String )
    {
        userName.value = newName
        updateUserNameModel(newName)
    }

    fun finishCreatingSymbol()
    {
        println("---------------------- Over Here motherfucker -----------------------------")
    }

    fun selectOption( levelIn: Int, item: OpenableMenuItem)
    {
        val a = selectedOptStack.value.toMutableList()
        if(levelIn < listStack.size - 1)
        {
            while (levelIn < listStack.size - 1)
            {
                println("---------------------- Over Here motherfucker2 -----------------------------")
                listStack.removeLast()
                a.removeLast()
            }
        }
        val newList = menuListsHelper.getList(item.id,levelIn) ?: return
        listStack.add(newList)
        a.add( mutableStateOf(newList[0]))
        selectedOptStack.value = a.toList()
    }
    fun clearOpts()
    {
        selectedOpts.value = emptyList()
    }

    companion object{

        fun create(context: Context): ViewModelProvider.Factory{

            return object : ViewModelProvider.Factory{
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    if( modelClass.isAssignableFrom(ServerInfoVM::class.java) )
                    {
                        return ServerInfoVM(context) as T
                    }
                    throw IllegalArgumentException("Unknown VM class")
                }
            }
        }
    }
}