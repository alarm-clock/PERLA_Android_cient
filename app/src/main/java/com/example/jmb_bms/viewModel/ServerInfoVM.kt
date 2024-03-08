package com.example.jmb_bms.viewModel

import android.annotation.SuppressLint
import android.content.ComponentName
import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.graphics.ImageBitmap
import androidx.core.content.edit
import androidx.lifecycle.*
import com.example.jmb_bms.connectionService.ConnectionService
import com.example.jmb_bms.connectionService.ConnectionState
import com.example.jmb_bms.connectionService.ServiceStateCallback
import com.example.jmb_bms.model.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow


@SuppressLint("StaticFieldLeak") //using application context which won't leak memory
class ServerInfoVM(context: Context,) : ViewModel(), ServiceStateCallback {

    private val applicationContext = context

    private var service : ConnectionService? = null

    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, serviceBin: IBinder?) {
            val binder = serviceBin as ConnectionService.LocalBinder
            service = binder.getService()
            service?.setCallBack(this@ServerInfoVM)
            // Now you can use myService to call methods on the service
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            service = null
        }
    }

    private lateinit var model: ServerInfo
    var ipv4 = MutableLiveData<String>("")
    val port = MutableLiveData<String>("")
    val userName = MutableLiveData<String>("")
    val everyThingEntered = MutableLiveData<Boolean>(false)
    val everyThingCorrect = MutableLiveData<Boolean>(checkIfEveryThingIsCorrect())

    private var ipv4Correct: Boolean = false
    private var portCorrect: Boolean = false
    private var userNameCorrect: Boolean = false
    private fun checkIfEveryThingIsCorrect() = ipv4Correct && portCorrect && userNameCorrect

    private var ipStoreJob : Job? = null
    private var portStoreJob : Job? = null
    private var nameStoreJob : Job? = null
    private val jobDelay: Long = 1000
    private val iconSize = (450).toString()

    private val _connectionState = MutableLiveData<ConnectionState>(ConnectionState.NOT_CONNECTED)
    val connectionState: LiveData<ConnectionState> = _connectionState

    private val _connectionErrorMsg = MutableLiveData("")
    val connectionErrorMsg: LiveData<String> = _connectionErrorMsg

    private val menuListsHelper = MenuListsHelper()

    val level1 = listOf(OpenableMenuItem("P","Space",BattleDimension.SPACE,null),
        OpenableMenuItem("A","AIR",BattleDimension.AIR,null),
        OpenableMenuItem("G","Ground",BattleDimension.GROUND,null),
        OpenableMenuItem("S","Sea Surface",BattleDimension.SEA_SURFACE,null),
        OpenableMenuItem("U","Sea Subsurface",BattleDimension.SEA_SUBSURFACE,null),
        OpenableMenuItem("F", "SOF", BattleDimension.SOF,null),
        OpenableMenuItem("X","Other",BattleDimension.OTHER,null)
        )

    val listStack = mutableListOf(level1)
    val selectedOptStack = MutableStateFlow(listOf(mutableStateOf(level1[0]))) //mutableStateOf(listOf(mutableStateOf( level1[0])))
    val bitMap = MutableLiveData<ImageBitmap?>(null) //mutableStateOf<ImageBitmap?>(null)

    val loading = MutableLiveData(true)

    var transitionManager: TeamSCTransitionManager? = null

    init {

        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                model = ServerInfo(context, context.getSharedPreferences("jmb_bms_Server_Info", MODE_PRIVATE))

                ipv4Correct = checkAndReformatValue(model.ipV4) != null
                portCorrect = checkAndReformatValue(model.portString) != null
                userNameCorrect = checkAndReformatValue(model.userName) != null
                everyThingCorrect.postValue(checkIfEveryThingIsCorrect())

                ipv4.postValue(model.ipV4)
                port.postValue(model.portString)
                userName.postValue(model.userName)
                everyThingEntered.postValue(model.everyThingEntered())
                bitMap.postValue(model.symbol.imageBitmap)
                prepareMenuFromTheString()
                loading.postValue(false)
            }
        }
    }

    //TODO maybe put this job on Dispatchers.IO scope for better performance
    private fun updateIPModel(newAddr: String)
    {
        ipStoreJob?.cancel()
        ipStoreJob = viewModelScope.launch {
            delay(jobDelay)
            model.ipV4 = newAddr
            everyThingEntered.value = model.everyThingEntered()
        }
    }
    private fun checkAndReformatValue(newAddress: String): String?
    {
        val trimed = newAddress.trim()

        return if(trimed.contains(' ')) null
        else trimed
    }
    fun updateIpAddress( newAddress: String)
    {
        ipv4.value = newAddress
        val trimmed = checkAndReformatValue(newAddress)

        ipv4Correct = trimmed != null
        everyThingCorrect.postValue(checkIfEveryThingIsCorrect())

        updateIPModel(trimmed ?: newAddress)
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
        val trimmed = checkAndReformatValue(newPort)

        portCorrect = trimmed != null
        everyThingCorrect.postValue(checkIfEveryThingIsCorrect())

        updatePortModel( trimmed ?: newPort)
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
        val trimmed = checkAndReformatValue(newName)

        userNameCorrect = trimmed != null
        everyThingCorrect.postValue(checkIfEveryThingIsCorrect())

        updateUserNameModel( trimmed ?: newName)
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

    fun finishCreatingSymbol(iconCode2525: IconCode2525, context: Context)
    {
        viewModelScope.launch {
            withContext(Dispatchers.IO)
            {
                val dimension = selectedOptStack.value.first().value.topLevelMod2525 ?: return@withContext

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
    }

    private fun prepareMenuFromTheString()
    {
        if(model.menuIdsString.isEmpty()) return

        val strList = model.menuIdsString.split('|').toMutableList()

        viewModelScope.launch {
            withContext(Dispatchers.Main)
            {
                strList.forEachIndexed { index, it ->
                    selectedOptStack.value[index].value = listStack[index].first{ listElement -> listElement.id == it  }
                    selectOption(index, selectedOptStack.value[index].value)
                }
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
    fun connect()
    {
        applicationContext.applicationInfo
        try {
            applicationContext.startForegroundService(Intent(applicationContext,ConnectionService::class.java)
                .putExtra("Caller","ServerInfoVM")
                .putExtra("Host",ipv4.value)
                .putExtra("Port",port.value?.toInt()))   // enclose it in try catch or chceck if port is valid
            bindService(applicationContext)
        } catch (e: Exception)
        {
            _connectionErrorMsg.postValue("Does not have permission to run service. Change that in settings...")
        }

    }
    fun disconnect()
    {
        unbindService(applicationContext)
        applicationContext.stopService(Intent(applicationContext,ConnectionService::class.java).putExtra("Caller","ServerInfoVM"))
    }

    fun bindService(context: Context) {
        val intent = Intent(context, ConnectionService::class.java).putExtra("Caller","ServerInfoVM")
        context.bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)
        Log.d("ServerInfoVm","Tried to set myself as callback. Session is: $service")
        _connectionState.postValue(service?.serviceModel?.connectionState ?: ConnectionState.NOT_CONNECTED)
        _connectionErrorMsg.postValue(service?.serviceModel?.errorString ?: "")
    }

    fun unbindService(context: Context) {
        service?.unSetCallBack()
        if(service != null) context.unbindService(serviceConnection)
        service = null
        //_connectionState.postValue(ConnectionState.NOT_CONNECTED)
    }
    override fun onCleared() {
        super.onCleared()
        val running =  applicationContext.getSharedPreferences("jmb_bms_Server_Info", MODE_PRIVATE).getBoolean("Service_Running",false)
        if(running) unbindService(applicationContext)

    }

    fun resetState()
    {
        _connectionState.postValue(ConnectionState.NOT_CONNECTED)
    }
    override fun onOnServiceStateChanged(newState: ConnectionState) {

        if(newState == _connectionState.value) return
        Log.d("ServerInfoVM","In onServiceStateChanged, values are:\nnewState: ${newState.name}")
        if(newState == ConnectionState.NEGOTIATING)
        {
            loading.postValue(true)
        } else
        {
            loading.postValue(false)
        }
        if(newState == ConnectionState.CONNECTED)
        {
            Log.d("ServerInfoVM", "trying to change screens")
            service?.unSetCallBack()
            unbindService(applicationContext)
            applicationContext.getSharedPreferences("jmb_bms_Server_Info", MODE_PRIVATE).edit {
                putBoolean("Server_Connected",true)
                apply()
            }
            transitionManager?.changingToServerVM()

        } else if( newState == ConnectionState.ERROR)
        {
            unbindService(applicationContext)
            applicationContext.stopService(Intent(applicationContext,ConnectionService::class.java))
        }
        _connectionState.postValue(newState)

    }
    override fun onServiceErroStringChange(new: String) {
        _connectionErrorMsg.postValue(new)
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