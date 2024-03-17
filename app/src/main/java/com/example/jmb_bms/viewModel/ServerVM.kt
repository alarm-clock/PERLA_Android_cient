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
import com.example.jmb_bms.LocusVersionHolder
import com.example.jmb_bms.connectionService.*
import com.example.jmb_bms.connectionService.in_app_communication.ComplexServiceStateCallBacks
import com.example.jmb_bms.connectionService.in_app_communication.ServiceStateCallback
import com.example.jmb_bms.connectionService.models.TeamProfile
import com.example.jmb_bms.connectionService.models.UserProfile
import com.example.jmb_bms.model.RefreshVals
import com.example.jmb_bms.model.TeamLiveDataHolder
import com.example.jmb_bms.model.TeamPairsHolder
import com.example.jmb_bms.model.utils.centerMapInLocusJson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import locus.api.android.utils.LocusUtils
import kotlin.concurrent.thread
import kotlin.coroutines.CoroutineContext

class ServerVM(context: Context) : ViewModel(), ServiceStateCallback, ComplexServiceStateCallBacks {

    val applicationContext = context

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

    private val _connectionState = MutableLiveData<ConnectionState>(ConnectionState.NONE)
    val connectionState: LiveData<ConnectionState> = _connectionState

    private val _connectionErrorMsg = MutableLiveData("")
    val connectionErrorMsg: LiveData<String> = _connectionErrorMsg

    val connectedUsers = MutableStateFlow(listOf<MutableLiveData<UserProfile>>())

    //val teams = MutableStateFlow(Collections.synchronizedSet( HashSet<Pair< MutableLiveData<TeamProfile>,MutableStateFlow<HashSet<MutableLiveData<UserProfile>>>>>()))

    val teams = TeamPairsHolder()

    private val _sharingLocation = MutableLiveData(false)
    val sharingLocation: LiveData<Boolean> = _sharingLocation

    private var connecting = true
    val checked = mutableStateOf(false)

    var ipv4: String
    var port: Int

    val refreshValues = RefreshVals.entries.toList()
    val pickedRefresh = mutableStateOf(refreshValues[0])

    val pickedTeam = mutableStateOf(
        TeamLiveDataHolder(
            Pair(
                TeamProfile("","","Pick a Team","",context, mutableSetOf()),
                mutableSetOf()
            ),
            mutableListOf()
        )
    )

    var userProfile: UserProfile? = null

    val shPref: SharedPreferences


    init {
        shPref = context.getSharedPreferences("jmb_bms_Server_Info", Context.MODE_PRIVATE)
        setCheck(shPref.getBoolean("Server_Checked",false))

        val p = shPref.getString("ServerInfo_Port","0") ?: "0"
        port = if(p == "") 0 else p.toInt()
        ipv4 = shPref.getString("ServerInfo_IP","") ?: ""


        prepareRefreshValFromShPref()

        val running =  shPref.getBoolean("Service_Running",false)
        Log.d("ServerVM","In init block before binding, running is $running")
        if(running) bindService()
        else setCheck(false)
    }

    private fun prepareRefreshValFromShPref()
    {
        val str = shPref.getString("Refresh_Val","5s") ?: "5s"
        pickedRefresh.value = refreshValues.find { it.menuString == str } ?: RefreshVals.S1
    }

    fun sethost(ipv4: String,port: Int)
    {
        this.ipv4 = ipv4
        this.port = port
    }


    fun selectTeamOption(value: TeamLiveDataHolder)
    {
        pickedTeam.value = value
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
        selectTeamOption( TeamLiveDataHolder(
            Pair(
                TeamProfile("","","Pick a Team","",applicationContext, mutableSetOf()),
                mutableSetOf()
            ),
            mutableListOf()
        ))
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
    fun toggleCheck()
    {
        if(checked.value) connecting = true
        setCheck(!checked.value)
    }

    private fun setCheck(newState: Boolean)
    {
        Log.d("ServerVM","SetCheck new value is: $newState")
        checked.value = newState
        viewModelScope.launch {
            withContext(Dispatchers.IO)
            {
                shPref.edit {
                    putBoolean("Server_Checked",checked.value)
                }
            }
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

    fun kickUserFromTeam(userId: String)
    {
        val teamId = pickedTeam.value.getTeamId()

        if(teamId == null)
        {
            _connectionErrorMsg.postValue("Could not get teamId from VM")
            return
        }
        service?.addOrDelUserMessage(teamId,userId,false)
    }
    fun toggleUsersLocationShare(userId: String)
    {
        val teamId = pickedTeam.value.getTeamId()
        if(teamId == null)
        {
            _connectionErrorMsg.postValue("Could not get teamId from VM")
            return
        }
        service?.handleTeamMemberLocationSharingReq(teamId,userId)
    }

    fun makeUserTeamLead(userId: String)
    {
        val teamId = pickedTeam.value.getTeamId()
        if(teamId == null)
        {
            _connectionErrorMsg.postValue("Could not get teamId from VM")
            return
        }
        service?.changeTeamLeaderMessage(teamId, userId)
    }
    override fun updatedUserListCallBack(newList: List<UserProfile>) {

        thread {
            val list = newList.map { profile ->
                connectedUsers.value.find { it.value?.serverId == profile.serverId } ?: MutableLiveData(profile)
            }
            viewModelScope.launch {
                withContext(Dispatchers.Main)
                {
                    connectedUsers.value = list
                    Log.d("ServerVM","Setting connectedUsers list: newList is $list")
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

        viewModelScope.launch {
            withContext(Dispatchers.Main)
            {
                if(newState == ConnectionState.CONNECTED || newState == ConnectionState.NEGOTIATING) setCheck(true)
                else
                {
                    setCheck(false)
                }
            }
        }
    }
    override fun setUsersAnTeams(newList: List<UserProfile>,newSet: MutableSet<Pair<TeamProfile,HashSet<UserProfile>>>)
    {
        val list = newList.map { profile ->
            connectedUsers.value.find { it.value?.serverId == profile.serverId } ?: MutableLiveData(profile)
        }
        val set = newSet.map { TeamLiveDataHolder(it,list) }.toHashSet()
        runOnThread(Dispatchers.Main){
            connectedUsers.value = list
            teams.setNewSet( set )
        }

    }
    override fun updateSharingLocationState(newState: Boolean) {
        _sharingLocation.postValue(newState)
    }

    override fun updateUserList(profile: UserProfile, add: Boolean) {

        thread {
            runOnThread(Dispatchers.Main){
                val tmp = connectedUsers.value.toMutableList()
                if(add) tmp.add(MutableLiveData(profile))
                else tmp.removeIf { it.value?.serverId == profile.serverId }
                connectedUsers.value = tmp
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
        liveProfile.postValue(profile.copy())
    }

    override fun clientProfile(profile: UserProfile) {
        userProfile = profile
       // TODO("Not yet implemented")
        //TODO firstly it will check if profile is client profile or someone else and then change things accordingly
    }


    override fun onServiceErroStringChange(new: String) {
        _connectionErrorMsg.postValue(new)
    }

    override fun setTeamsSet(newSet: MutableSet<Pair<TeamProfile,HashSet<UserProfile>>>)
    {
        Log.d("ServerVM","In setTeamsSet setting new set, connectedUsers is ${connectedUsers.value}")
        val set = newSet.map { TeamLiveDataHolder(it,connectedUsers.value) }.toHashSet()
        runOnThread(Dispatchers.Main){
            teams.setNewSet( set )
        }
    }

    override fun updateTeamsList(element: Pair<TeamProfile,HashSet<UserProfile>>, add: Boolean)
    {
        runOnThread(Dispatchers.Main)
        {
            teams.updateSet(TeamLiveDataHolder(element,connectedUsers.value),add)
        }
    }

    override fun updateTeamsProfile(element: TeamProfile)
    {
        val pair = teams.findPair(element._id)
        runOnThread(Dispatchers.Main)
        {
            pair?.updateTeamProfile(element)
        }
    }

    override fun updateTeammateList(teamId: String, profile: UserProfile, add: Boolean)
    {
        Log.d("ServerVM","In updateTeammateList with teamId: $teamId, and profile: $profile")
        val pair = teams.findPair(teamId)
        val existing = connectedUsers.value.find { it.value?.serverId == profile.serverId }
        Log.d("ServerVM","Existing is $existing")
        runOnThread(Dispatchers.Main){

            if(existing == null) pair?.updateTeamMembersList(profile,add)
            else pair?.updateTeamMembersList(existing,add)
        }
    }

    fun showUserInLocusMap(userProfile: UserProfile?)
    {
        userProfile ?: return
        centerMapToLocationAndStartLocus(userProfile.location!!.latitude,userProfile.location!!.longitude)
    }

    private fun centerMapToLocationAndStartLocus(lat: Double, long: Double)
    {
        val json = centerMapInLocusJson(lat,long)
        Log.d("ServerVm",json)
        val intent = Intent("com.asamm.locus.ACTION_TASK").apply {
            setPackage(LocusVersionHolder.getLv()!!.packageName)
            putExtra("tasks",json)
        }
        applicationContext.sendBroadcast(intent)
        LocusUtils.callStartLocusMap(applicationContext)
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