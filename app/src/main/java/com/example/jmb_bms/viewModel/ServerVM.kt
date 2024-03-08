package com.example.jmb_bms.viewModel

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.SharedPreferences
import android.os.IBinder
import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.core.content.edit
import androidx.lifecycle.*
import com.example.jmb_bms.connectionService.*
import com.example.jmb_bms.model.RefreshVals
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import kotlin.concurrent.thread

class ServerVM(context: Context) : ViewModel(), ServiceStateCallback, ComplexServiceStateCallBacks {

    private val applicationContext = context

    private var service : ConnectionService? = null
    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, serviceBin: IBinder?) {
            val binder = serviceBin as ConnectionService.LocalBinder
            service = binder.getService()

            Log.d("ServerVM","Setting myself as callback to $service")
            service?.setCallBack(this@ServerVM)
            service?.setComplexDataCallBack(this@ServerVM)
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            //_connectedUsers.postValue(null)
            val tmp = connectedUsers.value.toMutableList()
            tmp.removeAll { true }
            connectedUsers.value = tmp.toList()
            service = null
        }
    }

    private val _connectionState = MutableLiveData<ConnectionState>(ConnectionState.NOT_CONNECTED)
    val connectionState: LiveData<ConnectionState> = _connectionState

    private val _connectionErrorMsg = MutableLiveData("")
    val connectionErrorMsg: LiveData<String> = _connectionErrorMsg

    val connectedUsers = MutableStateFlow(listOf<MutableLiveData<UserProfile>>())

    private val _sharingLocation = MutableLiveData(false)
    val sharingLocation: LiveData<Boolean> = _sharingLocation

    val ipv4: String
    val port: Int

    val refreshValues = RefreshVals.entries.toList()
    val pickedRefresh = mutableStateOf(refreshValues[0])

    val shPref: SharedPreferences

    init {
        shPref = context.getSharedPreferences("jmb_bms_Server_Info", Context.MODE_PRIVATE)

        val p = shPref.getString("ServerInfo_Port","0") ?: "0"
        port = if(p == "") 0 else p.toInt()
        ipv4 = shPref.getString("ServerInfo_IP","") ?: ""

        prepareRefreshValFromShPref()

        val running =  shPref.getBoolean("Service_Running",false)
        Log.d("ServerVM","In init block before binding, running is $running")
        if(running) bindService()
    }

    private fun prepareRefreshValFromShPref()
    {
        val str = shPref.getString("Refresh_Val","1s") ?: "1s"
        pickedRefresh.value = refreshValues.find { it.menuString == str } ?: RefreshVals.S1
    }

    fun selectOption(value: RefreshVals)
    {
        pickedRefresh.value = value
        viewModelScope.launch {
            withContext(Dispatchers.IO)
            {
                shPref.edit {
                    putString("Refresh_Val",value.menuString)
                    apply()
                }
            }
            service?.changeDelayForLocSh(value.delay.inWholeMilliseconds)
        }
    }


    fun bindService() {

        if(service != null) return
        Log.d("ServerVM", "Binding to service")
        val intent = Intent(applicationContext, ConnectionService::class.java).putExtra("Caller","ServerVM")
        applicationContext.bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)

    }

    fun unbindService() {
        service?.unSetCallBack()
        service?.unSetComplexDataCallBack()
        Log.d("ServerVM","In unbindService function. Running is ")
        //removed running check
        if(service != null) applicationContext.unbindService(serviceConnection)
        service = null
    }

    fun reconnect()
    {
        if(service != null)
        {
            Log.d("ServerVM","Service is not null so trying to restart it by function")
            service?.restartSessionWithServer()
        } else
        {
            Log.d("ServerVM","Service is null so calling connect function")
            connect()
        }
    }

    fun startSharingLocation()
    {
        service?.startSharingLocation(pickedRefresh.value.delay.inWholeMilliseconds)
    }
    fun stopSharingLocation()
    {
        runBlocking {
            service?.stopSharingLocation()
        }
    }
    fun connect()
    {
        Log.d("ServerVM",if(service == null) "In connect function going to start activity" else "In connect function going to call reconnect")
        if(service != null) reconnect()
        else
        {
            Log.d("ServerVM","Starting service... or at least trying")
            applicationContext.startForegroundService(Intent(applicationContext,ConnectionService::class.java)
                .putExtra("Caller","ServerVM")
                .putExtra("Host",ipv4)
                .putExtra("Port",port))   // enclose it in try catch or chceck if port is valid
            bindService()
        }

    }

    fun changeConnnectionState()
    {
        if(connectionState.value == ConnectionState.NOT_CONNECTED || connectionState.value == ConnectionState.ERROR)
        {
            Log.d("ServerVM", "Calling connect")
            connect()
        } else
        {
            Log.d("ServerVM","Calling disconnect")
            disconnect()
        }
    }


    fun disconnect()
    {
        val tmp = connectedUsers.value.toMutableList()
        tmp.removeAll { true }
        connectedUsers.value = tmp.toList()

        unbindService()
        Log.d("ServerVM","Session is $service")
        applicationContext.stopService(Intent(applicationContext,ConnectionService::class.java).putExtra("Caller","ServerVM"))
        onOnServiceStateChanged(ConnectionState.NOT_CONNECTED)
        onServiceErroStringChange("")
        updateSharingLocationState(false)
    }

    //override fun updatedUserListCallBack(newList: List<UserProfile>) {

    override fun updatedUserListCallBack(newList: List<UserProfile>) {

        thread {
            viewModelScope.launch {
                withContext(Dispatchers.Main)
                {
                    connectedUsers.value = newList.map { MutableLiveData(it) }
                }
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        val tmp = connectedUsers.value.toMutableList()
        tmp.removeAll{true}
        connectedUsers.value = tmp

        unbindService()
    }

    override fun onOnServiceStateChanged(newState: ConnectionState) {
        if(newState == _connectionState.value) return
        Log.d("ServerVM","New State: $newState")
        _connectionState.postValue(newState)
    }

    override fun updateSharingLocationState(newState: Boolean) {
        _sharingLocation.postValue(newState)
    }

    override fun updateUserList(profile: UserProfile, add: Boolean) {

        thread {
            viewModelScope.launch {
                withContext(Dispatchers.Main)
                {
                    val tmp = connectedUsers.value.toMutableList()
                    if(add) tmp.add(MutableLiveData(profile))
                    else tmp.removeIf { it.value?.serverId == profile.serverId }
                    connectedUsers.value = tmp
                }
            }
        }
    }

    override fun profileChanged(profile: UserProfile) {
        val liveProfile = connectedUsers.value.find { it.value?.serverId == profile.serverId }
        if(liveProfile == null)
        {
            Log.d("ServerVM", "Somehow there is not stored profile which changed")
            return
        }
        liveProfile.postValue(profile)
    }


    override fun onServiceErroStringChange(new: String) {
        _connectionErrorMsg.postValue(new)
    }

    companion object{

        fun create(context: Context): ViewModelProvider.Factory{

            return object : ViewModelProvider.Factory{
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    if( modelClass.isAssignableFrom(ServerVM::class.java) )
                    {
                        return ServerVM(context) as T
                    }
                    throw IllegalArgumentException("Unknown VM class")
                }
            }
        }
    }


}