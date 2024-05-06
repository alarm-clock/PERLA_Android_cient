/**
 * @file: ServerInfoVM.kt
 * @author: Jozef Michal Bukas <xbukas00@stud.fit.vutbr.cz,jozefmbukas@gmail.com>
 * Description: File containing ServerInfoVM class
 */
package com.example.jmb_bms.viewModel.server

import android.annotation.SuppressLint
import android.content.ComponentName
import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import android.util.Log
import androidx.core.content.edit
import androidx.lifecycle.*
import com.example.jmb_bms.connectionService.ConnectionService
import com.example.jmb_bms.connectionService.ConnectionState
import com.example.jmb_bms.connectionService.in_app_communication.ServiceStateCallback
import com.example.jmb_bms.model.*
import com.example.jmb_bms.model.icons.SymbolCreationVMHelper
import kotlinx.coroutines.*
import kotlin.coroutines.CoroutineContext

/**
 * ViewModel for inputting server information and user information before connecting to server. Also implements [ServiceStateCallback] interface
 * @param context Application context for icon rendering
 * @constructor On IO coroutine prepares model and sets all live attributes with values from initialized model.
 * After that sets [loading] attribute to false so loading animation stops
 */
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

    var symbolCreationVMHelper : SymbolCreationVMHelper = SymbolCreationVMHelper(everyThingEntered){ context, runnable ->
        runOnThread(context,runnable)
    }

    val loading = MutableLiveData(true)

    var transitionManager: TeamSCTransitionManager? = null

    init {

        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                model = ServerInfo(context, context.getSharedPreferences("jmb_bms_Server_Info", MODE_PRIVATE))

                symbolCreationVMHelper.model = model
                ipv4Correct = checkAndReformatValue(model.ipV4) != null
                portCorrect = checkAndReformatValue(model.portString) != null
                userNameCorrect = checkAndReformatValue(model.userName) != null
                everyThingCorrect.postValue(checkIfEveryThingIsCorrect())

                ipv4.postValue(model.ipV4)
                port.postValue(model.portString)
                userName.postValue(model.userName)
                everyThingEntered.postValue(model.everyThingEntered())
                symbolCreationVMHelper.bitMap.postValue(model.symbol.imageBitmap)
                symbolCreationVMHelper.prepareMenuFromTheString()

                loading.postValue(false)
            }
        }
    }

    /**
     * Method for storing host onto model and checks if everything was entered with small delay if user is writing so that
     * after users stops writing it new value will be stored.
     * @param newAddr New host for storing
     */
    private fun updateIPModel(newAddr: String)
    {
        ipStoreJob?.cancel()
        ipStoreJob = viewModelScope.launch {
            delay(jobDelay)
            model.ipV4 = newAddr
            everyThingEntered.value = model.everyThingEntered()
        }
    }

    /**
     * Method for checking if entered value is correct and does not contain any space between words
     * @param newAddress Value for checking
     * @return Null if [newAddress] is incorrect value otherwise trimmed [newAddress]
     */
    private fun checkAndReformatValue(newAddress: String): String?
    {
        val trimed = newAddress.trim()

        return if(trimed.contains(' ')) null
        else trimed
    }

    /**
     * Method which stores host string written by user into [model] but also into [ipv4]
     * @param newAddress New value
     */
    fun updateIpAddress( newAddress: String)
    {
        ipv4.value = newAddress
        val trimmed = checkAndReformatValue(newAddress)

        ipv4Correct = trimmed != null
        everyThingCorrect.postValue(checkIfEveryThingIsCorrect())

        updateIPModel(trimmed ?: newAddress)
    }

    /**
     * Method for storing port onto model and checks if everything was entered with small delay if user is writing so that
     * after users stops writing it new value will be stored.
     * @param newPort New port for storing
     */
    private fun updatePortModel(newPort: String)
    {
        portStoreJob?.cancel()
        portStoreJob = viewModelScope.launch {
            delay(jobDelay)
            model.portString = newPort
            everyThingEntered.value = model.everyThingEntered()
        }
    }

    /**
     * Method which stores port string written by user into [model] but also into [port]
     * @param newPort New value
     */
    fun updatePort( newPort: String)
    {
        port.value = newPort
        val trimmed = checkAndReformatValue(newPort)

        portCorrect = trimmed != null
        everyThingCorrect.postValue(checkIfEveryThingIsCorrect())

        updatePortModel( trimmed ?: newPort)
    }

    /**
     * Method for storing name onto model and checks if everything was entered with small delay if user is writing so that
     * after users stops writing it new value will be stored.
     * @param newName New name for storing
     */
    private fun updateUserNameModel(newName: String)
    {
        nameStoreJob?.cancel()
        nameStoreJob = viewModelScope.launch {
            delay(jobDelay)
            model.userName = newName
            everyThingEntered.value = model.everyThingEntered()
        }
    }

    /**
     * Method which stores name string written by user into [model] but also into [userName]
     * @param newName New value
     */
    fun updateUserName( newName: String )
    {
        userName.value = newName
        val trimmed = checkAndReformatValue(newName)

        userNameCorrect = trimmed != null
        everyThingCorrect.postValue(checkIfEveryThingIsCorrect())

        updateUserNameModel( trimmed ?: newName)
    }

    /**
     * Method for starting service which then tries to connect to server. After service is started, [ServerInfoVM] is then bound to
     */
    fun connect()
    {
        //applicationContext.applicationInfo
        try {
            Log.d("ServerInfoVM","here")
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


    /**
     * Method for binding to service
     * @param context Context for binding to service
     */
    fun bindService(context: Context) {
        val intent = Intent(context, ConnectionService::class.java).putExtra("Caller","ServerInfoVM")

        context.bindService(intent, serviceConnection, 0) // zero flag so that new service is not started if non is running

        Log.d("ServerInfoVm","Tried to set myself as callback. Session is: $service")
        _connectionState.postValue(service?.serviceModel?.connectionState ?: ConnectionState.NOT_CONNECTED)
        _connectionErrorMsg.postValue(service?.serviceModel?.errorString ?: "")
    }

    /**
     * Method for unbinding service from vm
     * @param context Context for unbinding
     */
    fun unbindService(context: Context) {
        service?.unSetCallBack()
        if(service != null) context.unbindService(serviceConnection)
        service = null
        _connectionState.postValue(ConnectionState.NOT_CONNECTED)
    }
    override fun onCleared() {
        super.onCleared()
        val running =  applicationContext.getSharedPreferences("jmb_bms_Server_Info", MODE_PRIVATE).getBoolean("Service_Running",false)

        //if service is running then unbind
        if(running) unbindService(applicationContext)

    }

    /**
     * Method that sets default value to [_connectionState]
     */
    fun resetState()
    {
        _connectionState.postValue(ConnectionState.NOT_CONNECTED)
    }

    override fun onOnServiceStateChanged(newState: ConnectionState) {

        //sometimes same value which is already present is so if it is same value just ignore it
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
            transitionManager?.changingToServerVM(model.ipV4,model.port)

        } else if( newState == ConnectionState.ERROR)
        {
            //if error occurred there is no reason to keep service alive
            unbindService(applicationContext)
            applicationContext.stopService(Intent(applicationContext,ConnectionService::class.java))
        }
        _connectionState.postValue(newState)

    }
    override fun onServiceErroStringChange(new: String) {
        _connectionErrorMsg.postValue(new)
    }

    fun runOnThread(coroutineContext: CoroutineContext, runnable: suspend () -> Unit)
    {
        viewModelScope.launch {
            withContext(coroutineContext){
                runnable()
            }
        }
    }

    companion object{

        /**
         * Static method for creating custom vm factory for [ServerInfoVM] viewModel with custom parameters
         * @param context Application context for starting, stopping, binding, and unbinding [ConnectionService]
         */
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