/**
 * @file: ServerVM.kt
 * @author: Jozef Michal Bukas <xbukas00@stud.fit.vutbr.cz,jozefmbukas@gmail.com>
 * Description: File containing ServerVM class
 */
package com.example.jmb_bms.viewModel.server

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
import com.example.jmb_bms.model.icons.Symbol
import com.example.jmb_bms.model.utils.centerMapInLocusJson
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import locus.api.android.utils.LocusUtils
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.CopyOnWriteArraySet
import kotlin.concurrent.thread
import kotlin.coroutines.CoroutineContext

/**
 * ViewModel for server screen. It implements many methods from controlling service and server connection through location sharing control.
 * It also holds users and teams profiles. It also serves as team detail screen because it basically needs same data as normal server screen.
 * @param context Application context for controlling connection and rendering icons
 * @param pickedTeamId ID of picked team if this vm servers as vm for team detail
 * @constructor Prepares screen from shared preferences and binds to [ConnectionService] if it is running
 */
class ServerVM(context: Context, pickedTeamId: String?) : ViewModel(), ServiceStateCallback, ComplexServiceStateCallBacks {

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
                TeamProfile("","","Pick a Team","",context, null,mutableSetOf()),
                mutableSetOf()
            ),
            mutableListOf()
        )
    )
    val pickedTeamId: String? = pickedTeamId

    var userProfile: UserProfile? = null

    val shPref: SharedPreferences

    val teamEditingVM = TeamEditingVM(this){ context, code ->
        runOnThread(context,code)
    }

    val addingUsersVM = AddingUsersToTeamVM(this)


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

    /**
     * Method which prepares location refresh value for [pickedRefresh] attribute
     */
    private fun prepareRefreshValFromShPref()
    {
        val str = shPref.getString("Refresh_Val","5s") ?: "5s"
        pickedRefresh.value = refreshValues.find { it.menuString == str } ?: RefreshVals.S1
    }

    /**
     * Method that sets host and port
     * @param ipv4
     * @param port
     */
    fun sethost(ipv4: String,port: Int)
    {
        this.ipv4 = ipv4
        this.port = port
    }

    /**
     * Method that sets [pickedTeam] attribute
     * @param value Object with team data
     */
    fun selectTeamOption(value: TeamLiveDataHolder)
    {
        pickedTeam.value = value
    }

    /**
     * This method sets [pickedRefresh] attribute value, stores it shared preferences, and finally changes location refresh rate
     * in [service]
     * @param value New location refresh value
     */
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


    /**
     * Method for binding to service
     */
    fun bindService() {

        if(service != null) return
        Log.d("ServerVM", "Binding to service")
        val intent = Intent(applicationContext, ConnectionService::class.java).putExtra("Caller","ServerVM")
        applicationContext.bindService(intent, serviceConnection, 0)

        //set default picked team value in case something went wrong
        selectTeamOption( TeamLiveDataHolder(
            Pair(
                TeamProfile("","","Pick a Team","",applicationContext,null, mutableSetOf()),
                mutableSetOf()
            ),
            mutableListOf()
        ))
    }

    /**
     * Method for unbinding that just unbinds from service without checking anything
     */
    fun forceUnbind()
    {
        service?.unSetCallBack()
        service?.unSetComplexDataCallBack()
        Log.d("ServerVM","In unbindService function ")
        applicationContext.unbindService(serviceConnection)
        service = null
    }

    /**
     * Method for unbinding from service. If connection with server is in error state [ConnectionService] will be stopped
     */
    fun unbindService() {
        service?.unSetCallBack()
        service?.unSetComplexDataCallBack()
        Log.d("ServerVM","In unbindService function ")
        //removed running check
        if(service != null) {
            if(/*connectionState.value == ConnectionState.NOT_CONNECTED ||*/ connectionState.value == ConnectionState.ERROR)
            {
                applicationContext.stopService(Intent(applicationContext,ConnectionService::class.java))
            }
            applicationContext.unbindService(serviceConnection)
        }
        service = null
    }

    /**
     * Method for reconnecting to server. If [service] is running and bound, [ConnectionService.restartSessionWithServer]
     * method will be called. Otherwise [connect] method will be invoked.
     */
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

    /**
     * Method for toggling connection switch [checked] value and invoke [setCheck] method with new value.
     */
    fun toggleCheck()
    {
        if(checked.value) connecting = true
        setCheck(!checked.value)
    }

    /**
     * Method for setting connection to server switch [checked] value and storing it in shared preferences
     * @param newState New value
     */
    private fun setCheck(newState: Boolean)
    {
        Log.d("ServerVM","SetCheck new value is: $newState")
        checked.value = newState
        viewModelScope.launch {
            withContext(Dispatchers.Main)
            {
                shPref.edit {
                    putBoolean("Server_Checked",checked.value)
                }
            }
        }
    }

    /**
     * Method that invokes [ConnectionService.startSharingLocation] method with [pickedRefresh] value
     */
    fun startSharingLocation()
    {
        service?.startSharingLocation(pickedRefresh.value.delay.inWholeMilliseconds)
    }

    /**
     * Method for stopping location sharing. It invokes [ConnectionService.stopSharingLocation] method
     */
    fun stopSharingLocation()
    {
        runBlocking {
            service?.stopSharingLocation()
        }
    }

    /**
     * Method for starting [ConnectionService] and binding to it.
     */
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

    /**
     * Method which invokes [disconnect] method if service is connected to server (connection state is [ConnectionState.CONNECTED])
     * else it invokes [connect] for starting [ConnectionService] which then connects to server. Because connection switch in view copies
     * [_connectionState] this method can be invoked when user toggles it and correct method is always invoked.
     */
    fun changeConnnectionState()
    {
        if(connectionState.value == ConnectionState.NOT_CONNECTED || connectionState.value == ConnectionState.ERROR || connectionState.value == ConnectionState.NONE)
        {
            Log.d("ServerVM", "Calling connect")
            connect()
        } else
        {
            Log.d("ServerVM","Calling disconnect")
            disconnect()
        }
    }

    /**
     * Method for stopping service which also terminates connection with server. After that it sets default value for view
     * through [ComplexServiceStateCallBacks]
     */
    fun disconnect()
    {
        val tmp = connectedUsers.value.toMutableList()
        tmp.removeAll { true }
        connectedUsers.value = tmp.toList()

        forceUnbind()
        Log.d("ServerVM","Session is $service")
        applicationContext.stopService(Intent(applicationContext,ConnectionService::class.java).putExtra("Caller","ServerVM"))
        onOnServiceStateChanged(ConnectionState.NOT_CONNECTED)
        onServiceErroStringChange("")
        updateSharingLocationState(false)
        Log.d("HERE10","HERE10")
    }

    /**
     * Method that returns all users that are not in team identified by [teamId]
     * @param teamId ID of team
     * @return List of user profiles that are not on team with ID [teamId]
     */
    fun getUsersWhichAreNotOnTeam(teamId: String) = connectedUsers.value.filter {
        it.value?.teamEntry?.find { teamIds -> teamIds == teamId  } == null
    }


    /**
     * Method that removes user identified by [userId] from team by invoking [ConnectionService.addOrDelUserMessage]
     * with add parameter set to false if vm is bound to [ConnectionService]. TeamId is used from [pickedTeam] attribute.
     * @param userId ID user which should be kicked from team.
     */
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

    /**
     * Method that toggles location sharing for user identified by [userId], by invoking [ConnectionService.handleTeamMemberLocationSharingReq] method.
     * This method only sends request to server that handles location share toggle logic. If service is not running,
     * this method will not invoke anything.
     * @param userId ID of user whose location sharing is being toggled
     */
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

    /**
     * Method that makes user identified by [userId] team leader by invoking [ConnectionService.changeTeamLeaderMessage] method.
     * This method only sends request to server and that handles logic. If service is not running,
     * this method will not invoke anything. ID of the team is taken from [pickedTeam]
     * @param userId ID of user who is being set to be new team leader.
     */
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

    /**
     * Method fur updating team name and icon by invoking [ConnectionService.teamUpdateMessage] method. This method only sends request to server
     * and that handles logic. If service is not running, this method will not invoke anything.
     * ID of the team is taken from [pickedTeam].
     * @param newName New team name
     * @param newIcon New team icon
     */
    fun updateTeamNameAndIcon(newName: String, newIcon: String)
    {
        val teamId = pickedTeam.value.getTeamId()
        if(teamId == null)
        {
            _connectionErrorMsg.postValue("Could not get teamId from VM")
            return
        }
        service?.teamUpdateMessage(teamId, newName, newIcon)
    }

    /**
     * Method that adds user to team by invoking [ConnectionService.addOrDelUserMessage] method with add parameter set to true
     * @param teamId ID of team where user should be added
     * @param userId ID of user who is added to team
     */
    fun addUserToTeam(teamId: String,userId: String)
    {
        service?.addOrDelUserMessage(teamId,userId,true)
    }

    /**
     * Method that updates team location sharing by invoking [ConnectionService.handleTeamLocShState] method. This method only sends request to server
     * and that handles logic. If service is not running, this method will not invoke anything.
     * @param on Flag indicating if location sharing should be turned on or off
     */
    fun updateTeamLocSh(on: Boolean)
    {
        service?.handleTeamLocShState(pickedTeam.value.getTeamId()!!,on)
    }

    /**
     * Method that deletes team by invoking [ConnectionService.deleteTeamMessage] method. TeamId is taken from
     * [pickedTeam] attribute. This method only sends request to server
     * and that handles logic. If service is not running, this method will not invoke anything.
     */
    fun deleteTeam()
    {
        service?.deleteTeamMessage(pickedTeam.value.getTeamId()!!)
    }

    /**
     * Method that start location sharing as a team identified by [pickedTeam] ID. If service is not in running state nothing happens.
     * @param delay Location sharing period
     */
    fun startSharingLocAsTeam(delay: RefreshVals)
    {
        service?.teamModel?.startSharingLocationAsTeam(pickedTeam.value.getTeamId()!!,delay)
    }

    /**
     * Method that stops sharing location as a team.
     */
    fun stopSharingLocAsTeam()
    {
        service?.teamModel?.stopSharingLocationAsTeam(pickedTeam.value.getTeamId()!!)
    }

    /**
     * Method that changes location sharing as a team interval to desired value.
     * @param delay New delay
     */
    fun changeTeamLocShDelay(delay: RefreshVals)
    {
        service?.teamModel?.changeLocShDelay(pickedTeam.value.getTeamId()!!,delay)
    }
    override fun updatedUserListCallBack(newList: List<UserProfile>) {

        thread {
            // render symbol for every profile
            newList.forEach {
                it.symbol = Symbol(it.symbolCode,applicationContext)
            }
            //if user profile has existing live profile use that otherwise create new
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

    /**
     * Method that nullifies all symbols attached to user profiles just to be sure.
     */
    private fun nullifySymbols()
    {
        connectedUsers.value.map { it.value }.forEach {
            it?.symbol = null
        }
    }


    override fun onCleared() {
        super.onCleared()
        nullifySymbols()
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
                //if service is connected set switch state accordingly
                if(newState == ConnectionState.CONNECTED || newState == ConnectionState.NEGOTIATING) setCheck(true)
                else setCheck(false)
            }
        }
    }
    override fun setUsersAnTeams(newList: CopyOnWriteArrayList<UserProfile>,newSet: CopyOnWriteArraySet<Pair<TeamProfile,CopyOnWriteArraySet<UserProfile>>>)
    {
        val copy = connectedUsers.value.toHashSet()

        //render all symbols
        newList.forEach {
            it.symbol = Symbol(it.symbolCode,applicationContext)
        }
        //use existing live profiles otherwise create new
        val list = newList.map { profile ->
            copy.find { it.value?.serverId == profile.serverId } ?: MutableLiveData(profile)
        }
        val set = newSet.map { TeamLiveDataHolder(it,list) }.toHashSet()

        var team: TeamLiveDataHolder? = null

        //if vm is used for team detail then find data about picked team
        if(pickedTeamId != null)
        {
            team = set.find{ it.getTeamId() == pickedTeamId }
        }
        runOnThread(Dispatchers.Main){
            connectedUsers.value = list
            teams.setNewSet( set )

            if( team != null)
            {
                pickedTeam.value = team
            }
        }


    }
    override fun updateSharingLocationState(newState: Boolean) {
        _sharingLocation.postValue(newState)
    }

    override fun updateUserList(profile: UserProfile, add: Boolean) {


        CoroutineScope(Dispatchers.Main).launch {

            //this must happen on main thread otherwise connectedUser might change and that would lead to exception
            //tmp is copy of list in connectedUsers because if original is used, view change isn't triggered
            val tmp = connectedUsers.value.toMutableList()
            CoroutineScope(Dispatchers.IO).launch {
                if(add) {
                    profile.symbol = Symbol(profile.symbolCode,applicationContext)
                    tmp.add(MutableLiveData(profile))
                }
                else tmp.removeIf { it.value?.serverId == profile.serverId }

                CoroutineScope(Dispatchers.Main).launch {
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
        //profile copy is done so that view redraw is triggered
        val cp = profile.copy()
        cp.symbol = Symbol(profile.symbolCode,applicationContext)
        liveProfile.postValue(cp)
    }

    override fun clientProfile(profile: UserProfile) {
        Log.d("ServerVM","In user profile function ????????????????????????")
        userProfile = profile
       // TODO("Not yet implemented")
        //TODO firstly it will check if profile is client profile or someone else and then change things accordingly
    }


    override fun onServiceErroStringChange(new: String) {
        _connectionErrorMsg.postValue(new)
    }

    override fun setTeamsSet(newSet: MutableSet<Pair<TeamProfile,CopyOnWriteArraySet<UserProfile>>>)
    {
        Log.d("ServerVM","In setTeamsSet setting new set, connectedUsers is ${connectedUsers.value}")
        val set = newSet.map { TeamLiveDataHolder(it,connectedUsers.value) }.toHashSet()
        runOnThread(Dispatchers.Main){
            teams.setNewSet( set )
        }
    }

    override fun updateTeamsList(element: Pair<TeamProfile,CopyOnWriteArraySet<UserProfile>>, add: Boolean)
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

    /**
     * Method that centers map on users location and starts Locus Map
     * @param userProfile User profile with location that will be shown on map
     */
    fun showUserInLocusMap(userProfile: UserProfile?)
    {
        userProfile ?: return
        centerMapToLocationAndStartLocus(userProfile.location!!.latitude,userProfile.location!!.longitude)
    }

    /**
     * Method that centers map on specified location in Locus Map and then starts it
     * @param lat Latitude
     * @param long Longitude
     */
    private fun centerMapToLocationAndStartLocus(lat: Double, long: Double)
    {
        val json = centerMapInLocusJson(lat,long)

        //create intent with action task json that will center map
        val intent = Intent("com.asamm.locus.ACTION_TASK").apply {
            setPackage(LocusVersionHolder.getLv()!!.packageName)
            putExtra("tasks",json)
        }
        applicationContext.sendBroadcast(intent)
        LocusUtils.callStartLocusMap(applicationContext)
    }

    /**
     * Method to launch coroutines
     * @param coroutineContext
     * @param runnable Peace of code that will be run
     */
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
         * Static method for creating custom vm factory for [ServerVM] class with custom parameters
         * @param context Application context for controlling connection and rendering icons
         * @param teamId ID of picked team if this vm servers as vm for team detail
         * @return Factory for [ServerVM] vm
         */
        fun create(context: Context, teamId: String?): ViewModelProvider.Factory{

            return object : ViewModelProvider.Factory{
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    if( modelClass.isAssignableFrom(ServerVM::class.java) )
                    {
                        return ServerVM(context,teamId) as T
                    }
                    throw IllegalArgumentException("Unknown VM class")
                }
            }
        }
    }
}