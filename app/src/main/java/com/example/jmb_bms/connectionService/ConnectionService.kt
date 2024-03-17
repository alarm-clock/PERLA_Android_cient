package com.example.jmb_bms.connectionService

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.os.Binder
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.content.edit
import com.example.jmb_bms.connectionService.in_app_communication.ComplexServiceStateCallBacks
import com.example.jmb_bms.connectionService.in_app_communication.InnerCommunicationCentral
import com.example.jmb_bms.connectionService.in_app_communication.ServiceStateCallback
import com.example.jmb_bms.connectionService.models.ConnectionDataAndState
import com.example.jmb_bms.connectionService.models.TeamRelatedDataModel
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.websocket.*
import io.ktor.http.*
import io.ktor.websocket.*
import kotlinx.coroutines.cancel
import kotlinx.coroutines.isActive
import kotlinx.coroutines.runBlocking
import kotlin.concurrent.thread

class ConnectionService : Service() {

    private val comCentral: InnerCommunicationCentral = InnerCommunicationCentral()
    fun setCallBack(callback: ServiceStateCallback)
    {
        comCentral.registerStateCallBack(callback)
        callback.onOnServiceStateChanged(serviceModel.connectionState)
        callback.onServiceErroStringChange(serviceModel.errorString)

    }
    fun unSetCallBack()
    {
        comCentral.unRegisterStateCallBack()
    }

    fun setComplexDataCallBack(callBacks: ComplexServiceStateCallBacks)
    {
        comCentral.registerComplexCallBack(callBacks)
        callBacks.setUsersAnTeams(serviceModel.listOfUsers,teamModel.teams)
        //callBacks.updatedUserListCallBack(serviceModel.listOfUsers)
        callBacks.updateSharingLocationState(serviceModel.sharingLocation)
        //callBacks.setTeamsSet(teamModel.teams)
        callBacks.clientProfile(serviceModel.profile)
        //TODO add team related calls and client profile call
    }
    fun unSetComplexDataCallBack()
    {
        comCentral.unRegisterComplexCallBack()
    }


    private val binder = LocalBinder()
    override fun onBind(intent: Intent): IBinder {
        return binder
    }
    inner class LocalBinder : Binder() {
        fun getService(): ConnectionService = this@ConnectionService
    }

    private var connect = true
    private lateinit var client: HttpClient
    lateinit var session: DefaultWebSocketSession
        private set

    private var unknownFrameCounter = 0
    lateinit var serviceModel : ConnectionDataAndState
    lateinit var teamModel : TeamRelatedDataModel
    private var connectionThread: Thread? = null

    private var doesNotHavePermission = false

    //most likely this will not be used until session is created anyway
    private var sharingLocationJobHandler : PeriodicPositionUpdater? = null

    override fun onCreate() {
        super.onCreate()
        serviceModel = ConnectionDataAndState(this,comCentral)
        teamModel = TeamRelatedDataModel(comCentral,serviceModel,this)

        val channel = NotificationChannel("jmb_bms_1","jmb_bms_notification channel",NotificationManager.IMPORTANCE_DEFAULT)
        val notManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notManager.createNotificationChannel(channel)
        val notification = NotificationCompat.Builder(this,"jmb_bms_1").setContentTitle("Starting service...").build()

        try {
            startForeground(1,notification)
            registerConnectivityCallBack()

        } catch (e: Exception)
        {
            serviceModel.error = true
            serviceModel.connectionState = ConnectionState.ERROR
            serviceModel.errorString = "Does not have permission to run as service"
            Log.d("Service Connection", "Does not have permission to run as service")
            doesNotHavePermission = true
            Log.d("Service Connection", "Error in start foreground: ${e.message}")
        }
    }

    private fun registerConnectivityCallBack()
    {
        val conManager = getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager
        val req = NetworkRequest.Builder().addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET).build()

        val netCallback = object : ConnectivityManager.NetworkCallback() {

            private var firstCall = true
            private var lostWasCalled = false

            override fun onAvailable(network: Network) {
                super.onAvailable(network)
                if(firstCall)
                {
                    firstCall = false
                    return
                }
                Log.d("Connection Service","Network is available again so restarting connection...")
                this@ConnectionService.restartSessionWithServer()
                lostWasCalled = false
            }

            override fun onUnavailable() {
                super.onUnavailable()
                Log.d("JOJO","Over here motherfucker")
            }

            override fun onLost(network: Network) {
                super.onLost(network)

                if(!lostWasCalled) {
                    lostWasCalled = true
                    Log.d("Connection Service", "Lost internet connection. Stopping websocket!")
                    session.cancel()
                    sharingLocationJobHandler?.stopSharingLoc()
                }
            }
        }
        conManager.registerNetworkCallback(req,netCallback)
    }

    private fun setErrorState(errMsg: String)
    {
        Log.d("Connection service","Experiencing error!!\nMessage: $errMsg\n Setting connected to false, error to true, state to error")
        serviceModel.isConnected = false
        serviceModel.error = true
        serviceModel.connectionState = ConnectionState.ERROR
        serviceModel.errorString = errMsg
    }

    private suspend fun connectToServer()
    {
        //TODO here will go key exchange
        if(serviceModel.connectionState == ConnectionState.ERROR)
        {
            Log.d("Connection Service","Service model was not properly initialized so no way I'm connecting")
            serviceModel.isConnected = false
            return
        }
        Log.d("Connection service","Starting init sequence... State is negotiating")
        serviceModel.connectionState = ConnectionState.NEGOTIATING

        session.send(Frame.Text(ClientMessage.helloThere()))
        var answ = session.incoming.receive()
        if(answ.frameType != FrameType.TEXT )
        {
            session.send(Frame.Close())
            setErrorState("Received unknown frame from server")
            return
        }
        if(getOpcode(parseServerJson((answ as Frame.Text).readText())) != -1.0)
        {
            session.send(Frame.Close())
            setErrorState("Did not received correct answer for hello message")
            return
        }
        Log.d("Connection service","Received HELLO from server... Sending profile\n" +
                "Username: ${serviceModel.profile.userName}\n" +
                "Symbol: ${serviceModel.profile.symbolCode}\n" +
                "Id: ${serviceModel.profile.serverId}")
        session.send(Frame.Text(ClientMessage.userInfo(serviceModel.profile)))

        answ = session.incoming.receive()
        if(answ.frameType != FrameType.TEXT )
        {
            session.send(Frame.Close())
            setErrorState("Received unknown frame from server ")
            return
        }
        val response = parseServerJson((answ as Frame.Text).readText())
        if(getOpcode(response) != 1.0)
        {
            session.send(Frame.Close())
            setErrorState("Received incorrect response from server")
            return
        }
        Log.d("Connection service","Received response from server about profile")

        val param = response["_id"] as? String
        val teamEntry = response["teamEntry"] as? List<String>
        if(param == null || param == "")
        {
            session.send(Frame.Close())
            setErrorState("Invalid user name")
            return
        }
        Log.d("Connection service","Server id is: \"$param\". Storing it and setting state to Connected ")
        serviceModel.serverId = param
        serviceModel.profile.serverId = param
        serviceModel.profile.teamEntry = teamEntry?.toHashSet() ?: hashSetOf()
        serviceModel.isConnected = true
        serviceModel.connectionState = ConnectionState.CONNECTED

    }

    private fun parseBye(params: Map<String, Any?>)
    {
        connect = false
        serviceModel.errorString = params["reason"] as? String ?: "No reason given"
        serviceModel.connectionState = ConnectionState.NOT_CONNECTED

    }

    private fun parseTextFrame(message: String){
        val params = parseServerJson(message)
        val msgId = params["opCode"] as? Double
        Log.d("Connection service", "Parsing text frame. OpCode is $msgId")
        //runOnThread {
            when(msgId)
            {
                0.0 -> { parseBye(params)} //bye
                1.0 -> {} //this should never be called
                2.0 -> {
                    serviceModel.createUser(params, applicationContext)
                }
                3.0 -> serviceModel.updateUsersLocation(params, applicationContext)
                4.0 -> {}
                7.0 -> {
                    serviceModel.removeUserAndHisLocation(params["_id"] as? String ?: "" , applicationContext)
                }
                8.0 -> serviceModel.changeUserProfile(params, applicationContext)
                20.0 -> teamModel.createTeam(params)
                22.0 -> teamModel.deleteTeam(params)
                23.0 -> teamModel.manageTeamEntry(params)
                24.0 -> teamModel.changeTeamLeader(params)
                25.0 -> teamModel.updateTeam(params)
                26.0 -> runBlocking { serviceModel.manageLocationShareStateTeamWide(params) }
                28.0 -> runBlocking { serviceModel.manageIndividualLocationShareChange() }//TODO add logic to distinguish if it is team or user}
            }
        //}
    }
    private suspend fun parseMessage(frame: Frame)
    {
        when(frame){
            is Frame.Text -> { parseTextFrame( frame.readText())}
            is Frame.Close -> {
                val errMsg = frame.readReason()?.message
                Log.d("Websocket Close",errMsg?: "Reason for terminating connection was not given")
                serviceModel.errorString = errMsg ?: "Reason for terminating connection was not given"
                serviceModel.isConnected = false
                serviceModel.connectionState = ConnectionState.NOT_CONNECTED

            }
            else -> {
                unknownFrameCounter++
                if(unknownFrameCounter >= 6)
                {
                    session.send(Frame.Close(CloseReason(CloseReason.Codes.NOT_CONSISTENT,"Too many unknown frames")))
                    serviceModel.isConnected = false
                    serviceModel.error = true
                    serviceModel.errorString = "Too many unknown frames"
                    serviceModel.connectionState = ConnectionState.ERROR

                }
            }
        }
    }

    private fun connectAndRun(calledFromOnStart: Boolean)
    {
        Log.d("Connection Service","Connect and function was called from " + if(calledFromOnStart) "OnStartCommand" else "Restart" )
        client = HttpClient(CIO){
           // install(HttpTimeout)
            //{
              //  requestTimeoutMillis = 15000 // 15 seconds
             //   connectTimeoutMillis = 5000 // 5 seconds
              //  socketTimeoutMillis = 15000 // 15 seconds
            //}
            install(WebSockets) {
                //pingInterval = 15000
            }
        }
        runBlocking {
            try {
                Log.d("Service data","${serviceModel.host} ${serviceModel.port} ${this@ConnectionService}")
                client.ws(method = HttpMethod.Get,host = serviceModel.host, port = serviceModel.port, path = "/connect"){
                    session = this@ws
                    Log.d("Connection service","Initiating connection sequence")
                    connectToServer()
                    Log.d("Connection service","Sequnce completed... Connection state is: ${serviceModel.connectionState.name} ")
                    if(serviceModel.connectionState != ConnectionState.CONNECTED)
                    {
                        //if(calledFromOnStart) stopSelf()
                        return@ws
                    }
                    serviceModel.errorString = ""

                    if(!calledFromOnStart)
                    {
                        serviceModel.clearUsers()
                        teamModel.clearTeams()
                        if(serviceModel.sharingLocation)
                        {
                            //TODO just check if location stops
                            sharingLocationJobHandler?.startSharingLocation(serviceModel.period)
                        }
                    }

                    for( frame in incoming)
                    {
                        if(!connect){
                            Log.d("Connection service", "User initiated disconnect... Closing websocket")
                            send(Frame.Text(ClientMessage.bye("User disconnected")))
                            send(Frame.Close(CloseReason(CloseReason.Codes.NORMAL,"User disconnected")))
                            serviceModel.connectionState = ConnectionState.NOT_CONNECTED
                            serviceModel.isConnected = false
                            connect = false

                            Log.d("Connection service","Websocket is closed. Connection state is: ${serviceModel.connectionState.name}")
                            break
                        }
                        Log.d("Connection service","Parsing incoming frame")
                        parseMessage(frame)
                    }
                    Log.d("Connection Service","Out of the incoming loop")
                    if(serviceModel.connectionState != ConnectionState.ERROR) serviceModel.connectionState = ConnectionState.NOT_CONNECTED
                    serviceModel.isConnected = false

                }
            } catch (e: Exception)
            {
                //when server says error during init phase service will stop but when there will be exception service will survive
                Log.d("Connection service", "Experienced exception!\nMessage: ${e.message}\nStopping location share")
                sharingLocationJobHandler?.stopSharingLoc()
                serviceModel.sharingLocation = false


                Log.d("Connection service","Setting error state and sending to service state callback")
                serviceModel.errorString = e.message ?: "Encountered error"
                serviceModel.connectionState= ConnectionState.ERROR
                serviceModel.isConnected = false

            }
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        val caller = intent?.getStringExtra("Caller")
        //serviceModel = ConnectionDataAndState(this,comCentral)
        serviceModel.port = intent?.getIntExtra("Port",0) ?: 0
        serviceModel.host = intent?.getStringExtra("Host") ?: ""

        if(doesNotHavePermission)
        {
            Log.d("Connection Service","Can not run as service so I'm not starting")
            serviceModel.connectionState = ConnectionState.ERROR
            stopSelf()
        } else {

            Log.d("Service start", "Service was started by $caller")

            connectionThread = thread {
                connectAndRun(true)
            }.apply {
                name = "jmb_bms_server_session"
            }

            Log.d("Connection service", "Setting Running to true")
            this.getSharedPreferences("jmb_bms_Server_Info", Context.MODE_PRIVATE).edit {
                putBoolean("Service_Running", true)
                apply()
            }
        }

        return START_NOT_STICKY
    }

    fun restartSessionWithServer()
    {
        Log.d("Connection service","In restart function... connection thread is $connectionThread")
        if((connectionThread == null || connectionThread?.isAlive == false) && !doesNotHavePermission && !serviceModel.isConnected ) {
            serviceModel.error = false
            serviceModel.errorString = ""
            serviceModel.isConnected = false
            serviceModel.connectionState = ConnectionState.NOT_CONNECTED
            connect = true

            connectionThread = thread {
                connectAndRun(false)
            }.apply {
                name = "jmb_bms_server_session"
            }

        }
    }

    fun startSharingLocation(period: Long): Boolean
    {
        if( !this::session.isInitialized || !session.isActive)
        {
            serviceModel.locationShErrorString = "Can not start sharing location because there is no connection with server"
            return false
        }
        if( sharingLocationJobHandler == null)
        {
            serviceModel.period = period
            sharingLocationJobHandler = PeriodicPositionUpdater(period,this,session,true)
        } else {
            Log.d("Connection Service","sharingLocationJobHandler is not null")
            serviceModel.period = period
            sharingLocationJobHandler?.startSharingLocation(delay = period)
        }
        serviceModel.sharingLocation = true

        return true
    }

    suspend fun stopSharingLocation(){
        sharingLocationJobHandler?.stopSharingLoc()
        serviceModel.sharingLocation = false

        if(this::session.isInitialized)  session.send(Frame.Text(ClientMessage.stopUpdating()))

    }

    fun changeDelayForLocSh( newDelay: Long){
        serviceModel.period = newDelay
        sharingLocationJobHandler?.changeDelay(newDelay)
    }
    fun sendMessage(jsonString: String)
    {
        runOnThread {
            session.send(Frame.Text(jsonString))
        }
    }
    fun createTeamMessage(teamName: String, teamIcon: String, topTeamId: String)
    {
        sendMessage(ClientMessage.createTeam(teamName, teamIcon, topTeamId))
    }

    fun deleteTeamMessage(teamId: String)
    {
        sendMessage(ClientMessage.deleteTeam(teamId))
    }

    fun addOrDelUserMessage(teamId: String, userId: String, add: Boolean)
    {
        sendMessage(ClientMessage.addOrDelUser(teamId, userId, add))
    }

    fun changeTeamLeaderMessage(teamId: String, newLeaderId: String)
    {
        sendMessage(ClientMessage.changeTeamLeader(teamId, newLeaderId))
    }

    fun teamUpdateMessage(teamId: String, newName: String, newIcon: String)
    {
        sendMessage(ClientMessage.updateTeam(teamId,newName,newIcon))
    }

    fun handleTeamLocShState(teamId: String,on: Boolean)
    {
        sendMessage(ClientMessage.teamLocShSate(teamId, on))
    }

    fun handleTeamMemberLocationSharingReq(teamId: String,userId: String)
    {
        sendMessage(ClientMessage.userLocToggle(teamId, userId))
    }
    private fun runOnThread(code: suspend () -> Unit)
    {
        thread {
            runBlocking{
                code()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()


        Log.d("Connection Service", "In onDestroy")
        if(sharingLocationJobHandler != null)
        {
            Log.d("Connection Service","User was sharing location... Stopping it")
            sharingLocationJobHandler?.stopSharingLoc()
            serviceModel.sharingLocation = false

            Log.d("Connection Service","Stopped location sharing")
        }
        Log.d("Connection Service","Setting connect to false")
        connect = false
        if(this::session.isInitialized && session.isActive)
        {
            runBlocking {
                try {
                    Log.d("Connection Service","Trying to close connection with server")
                    session.send(Frame.Text(ClientMessage.bye("Ending session")))
                    session.send(Frame.Close(CloseReason(CloseReason.Codes.NORMAL,"Ending session")))
                } catch( e: Exception)
                {
                    Log.d("Connection Service","Closing connection encountered error!!\nMessage: ${e.message}")
                    //println(e.message)
                }
            }
        }
        serviceModel.isConnected = false
        serviceModel.connectionState = ConnectionState.NOT_CONNECTED

        Log.d("Connection Service","Setting running in shared preferences to false")
        this.getSharedPreferences("jmb_bms_Server_Info", Context.MODE_PRIVATE).edit {
            putBoolean("Service_Running",false)
            apply()
        }
            
        //deleting user points will be done manually so before ending
        //I must first copy user list into database or something
        //TODO for now I will delete them for the testing sake but when I will put in database I wont delete them here
        //TODO sometimes this throws concurrent modification exception so check on that

        try {
            teamModel.clearTeams()
            serviceModel.listOfUsers.forEach { profile ->
               // serviceModel.removeUserAndHisLocation(profile.serverId,this) //removeUserAndHisLocation(profile.serverId,this)
                serviceModel.removePoint(profile,this)
            }
            //serviceModel.listOfUsers.removeAll { true }
            serviceModel.clearUsers()
        } catch (e:Exception)
        {
            Log.d("Connection Service","Exception during removing...")
        }
    }
}