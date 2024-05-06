/**
 * @file: ConnectionService.kt
 * @author: Jozef Michal Bukas <xbukas00@stud.fit.vutbr.cz,jozefmbukas@gmail.com>
 * Description: File containing ConnectionService class
 */
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
import com.example.jmb_bms.connectionService.in_app_communication.*
import com.example.jmb_bms.connectionService.models.ChatRelatedModel
import com.example.jmb_bms.connectionService.models.ConnectionDataAndState
import com.example.jmb_bms.connectionService.models.PointRelatedDataModel
import com.example.jmb_bms.connectionService.models.TeamRelatedDataModel
import com.example.jmb_bms.model.ChatMessage
import com.example.jmb_bms.model.database.chat.ChatDBHelper
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.websocket.*
import io.ktor.http.*
import io.ktor.websocket.*
import kotlinx.coroutines.*
import java.security.cert.X509Certificate
import java.util.concurrent.CopyOnWriteArraySet
import javax.net.ssl.X509TrustManager
import kotlin.concurrent.thread

/**
 * Service used to manage connection with server. This service runs as foreground service. It is sticky service so when
 * it is stopped or destroyed by something else then [Context.stopService] call it will be restarted. Only exception
 * is when OS revokes all permission this service requires due to resource constrains. This service manages all communication
 * with server. That is live websocket session and all requests for uploading and downloading a files. Right after service
 * is created it tries to connect to server right away so count with that. [Intent] used to start this service can optionally
 * have extra "Host" [String] and "Port" [Int]. If there is no extra in received intent, host and port are taken from
 * shared preferences with name "jmb_bms_Server_Info" under "ServerInfo_IP" and "ServerInfo_Port". If there are no values
 * as well. Then service won't start. Because of using shared preferences as alternative way to pass host and port,
 * service therefore can start with null intent. To communicate with this service bind to it and use its methods directly.
 * If you want to receive updates like connection status or user point updates, register observing class using set...CallBack
 * methods. Note that only one observer can receive certain message types. It is required to unregister observer if
 * observer is about to be destroyed otherwise memory leak can occur. Service also can recover from loosing internet connection
 * and wait until it is available again and reconnect on its own. If error occurs when connection state is [ConnectionState.CONNECTED]
 * service is not stopped but waits until it is either destroyed or manually reconnected.
 */
class ConnectionService : Service() {

    val testing = true


    private val comCentral: InnerCommunicationCentral = InnerCommunicationCentral()

    /**
     * Method for setting [ServiceStateCallback] observer. Observed values are sent right away.
     * @param callback Observer
     */
    fun setCallBack(callback: ServiceStateCallback)
    {
        comCentral.registerStateCallBack(callback)
        callback.onOnServiceStateChanged(serviceModel.connectionState)
        callback.onServiceErroStringChange(serviceModel.errorString)

    }

    /**
     * Method for unsetting current [ServiceStateCallback] observer
     */
    fun unSetCallBack()
    {
        comCentral.unRegisterStateCallBack()
    }

    /**
     * Method for setting [ComplexServiceStateCallBacks] observer. Observed values are sent right away.
     * @param callBacks Observer
     */
    fun setComplexDataCallBack(callBacks: ComplexServiceStateCallBacks)
    {
        comCentral.registerComplexCallBack(callBacks)
        callBacks.setUsersAnTeams(serviceModel.listOfUsers,teamModel.teams)
        callBacks.updateSharingLocationState(serviceModel.sharingLocation)
        callBacks.clientProfile(serviceModel.profile)
    }

    /**
     * Method for setting [LiveUsersCallback] observer. Observed values are sent right away.
     * @param callback Observer
     */
    fun setLiveUsersCallBack(callback: LiveUsersCallback)
    {
        comCentral.registerLiveUsersCallback(callback)
        callback.updatedUserListCallBack(serviceModel.listOfUsers)
    }

    /**
     * Method for unsetting current [LiveUsersCallback] observer
     */
    fun unsetLiveUsersCallBack(){
        comCentral.unregisterLiveUsersCallback()
    }

    /**
     * Method for unsetting current [ComplexServiceStateCallBacks] observer
     */
    fun unSetComplexDataCallBack()
    {
        comCentral.unRegisterComplexCallBack()
    }

    /**
     * Method for setting [ChatRoomsCallBacks] observer. Observed values are sent right away.
     * @param callBacks Observer
     */
    fun setChatRoomsCallBack(callBacks: ChatRoomsCallBacks)
    {
        comCentral.registerChatRoomsCallback(callBacks)
        chatModel.fetchNewestMessages()
    }

    /**
     * Method for unsetting current [ChatRoomsCallBacks] observer
     */
    fun unsetChatRoomsCallBack()
    {
        comCentral.unregisterChatRoomsCallback()
    }

    /**
     * Method for setting [PointRelatedCallBacks] observer. Observed values are sent right away.
     * @param callbacks Observer
     */
    fun setPointCallBacks(callbacks: PointRelatedCallBacks)
    {
        comCentral.registerPointRelatedCallBacks(callbacks)
    }

    /**
     * Method for unsetting current [PointRelatedCallBacks] observer
     */
    fun unsetPointCallBacks()
    {
        comCentral.unregisterPointRelatedCallBacks()
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
    lateinit var pointModel: PointRelatedDataModel
    lateinit var chatModel: ChatRelatedModel
    private var connectionThread: Thread? = null

    private var doesNotHavePermission = false

    //most likely this will not be used until session is created anyway
    private var sharingLocationJobHandler : PeriodicPositionUpdater? = null

    /**
     * From class -
     * [android.app.Service] Called by the system when the service is first created. Do not call this method directly.
     *
     * Method which initializes service and calls [startForeground] at the end. It sets all models, prepares notification
     * channel, and finally registers connectivity callback to receive updates about network connection state.
     */
    override fun onCreate() {
        super.onCreate()
        serviceModel = ConnectionDataAndState(this,comCentral)
        teamModel = TeamRelatedDataModel(comCentral,serviceModel,this)
        pointModel = PointRelatedDataModel(this,comCentral)
        chatModel = ChatRelatedModel(this,comCentral)
        serviceModel.teamModel = teamModel

        val channel = NotificationChannel("jmb_bms_1","jmb_bms_notification channel",NotificationManager.IMPORTANCE_DEFAULT)
        val notManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notManager.createNotificationChannel(channel)
        val notification = NotificationCompat.Builder(this,"jmb_bms_1").setContentTitle("Starting service...").build()


        for(cnt in 0..10)
        {
            try {
                startForeground(1,notification)
                registerConnectivityCallBack()
                break

            } catch (e: Exception)
            {
                serviceModel.error = true
                serviceModel.connectionState = ConnectionState.ERROR
                serviceModel.errorString = "Does not have permission to run as service"
                Log.d("Service Connection", "Does not have permission to run as service")
                doesNotHavePermission = true
                Log.d("Service Connection", "Error in start foreground: ${e.message}")
                //Delay(1000)
            }
        }
    }
    private var conManager : ConnectivityManager? = null
    private var netCallback: ConnectivityManager.NetworkCallback? = null

    /**
     * Method for registering connectivity callback to receive connection updates.
     */
    private fun registerConnectivityCallBack()
    {
        conManager = getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager

        //only ability to reach internet is what need to know
        val req = NetworkRequest.Builder().addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET).build()

        netCallback = object : ConnectivityManager.NetworkCallback() {

            // onAvailable method is called when this callback is registered, so we want to ignore it
            private var firstCall = true

            //these methods can be called multiple times but the first call is what is important
            private var onAvailableWasCalled = false
            private var lostWasCalled = false

            override fun onAvailable(network: Network) {
                super.onAvailable(network)
                if(firstCall)
                {
                    firstCall = false
                    return
                }
                if(onAvailableWasCalled) return
                Log.d("Connection Service","Network is available again so restarting connection...")
                this@ConnectionService.restartSessionWithServer()
                lostWasCalled = false
                onAvailableWasCalled = true
            }

            override fun onUnavailable() {
                super.onUnavailable()
            }

            override fun onLost(network: Network) {
                super.onLost(network)

                if(!lostWasCalled) {
                    lostWasCalled = true
                    onAvailableWasCalled = false
                    Log.d("Connection Service", "Lost internet connection. Stopping websocket!")
                    //session.cancel()
                    sharingLocationJobHandler?.stopSharingLoc()
                    teamModel.teamLocationUpdateHandlers.forEach {
                        it.stopSharingLoc()
                    }
                }
            }
        }
        conManager?.registerNetworkCallback(req,netCallback!!)
    }

    /**
     * Method for setting error state in [serviceModel]
     * @param errMsg [String] with error message
     */
    private fun setErrorState(errMsg: String)
    {
        Log.d("Connection service","Experiencing error!!\nMessage: $errMsg\n Setting connected to false, error to true, state to error")
        serviceModel.isConnected = false
        serviceModel.error = true
        serviceModel.connectionState = ConnectionState.ERROR
        serviceModel.errorString = errMsg
    }

    /**
     * Method for handling connection procedure with server. If any error occurs [setErrorState] method is invoked
     * and error state is set. Otherwise, user profile is sent to server and ID is returned from server. If everything
     * goes as it should at the end of this phase connection state will be [ConnectionState.CONNECTED]
     */
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
        serviceModel.profile.teamEntry =  CopyOnWriteArraySet(teamEntry?.toHashSet() ?: setOf())
        serviceModel.isConnected = true
        serviceModel.connectionState = ConnectionState.CONNECTED

    }

    /**
     * Method for parsing bye message (opCode 0)
     * @param params JSON parsed into [Map]<[String],[Any]?> where field name is used as key
     */
    private fun parseBye(params: Map<String, Any?>)
    {
        connect = false
        serviceModel.errorString = params["reason"] as? String ?: "No reason given"
        //if(serviceModel.errorString == "Ping timeout")
        serviceModel.connectionState = ConnectionState.NOT_CONNECTED

    }

    /**
     * Method for parsing text frame. This is second method in "router" cascade of methods. This method invokes correct
     * method to parse text frame based on the opCode/msgId that is a number assigned to message. It also parses [message]
     * parameter into [Map]<[String],[Any]?> for unified and easier access to JSON fields.
     * @param message JSON [String] with message from server
     */
    private fun parseTextFrame(message: String){
        val params = parseServerJson(message)
        //Log.d("MSG",message)
        val msgId = params["opCode"] as? Double
        //Log.d("Connection service", "Parsing text frame. OpCode is $msgId")
        try {
            when(msgId?.toInt())
            {
                0 -> { parseBye(params)} //bye
                1 -> {} //this should never be called
                2 -> {
                    serviceModel.createUser(params, applicationContext)
                }
                3 -> serviceModel.updateUsersLocation(params, applicationContext)
                4 -> {}
                7 -> {
                    serviceModel.removeUserAndHisLocation(params["_id"] as? String ?: "" , applicationContext)
                }
                8 -> serviceModel.changeUserProfile(params, applicationContext)
                20 -> teamModel.createTeam(params)
                22 -> teamModel.deleteTeam(params)
                23 -> teamModel.manageTeamEntry(params)
                24 -> teamModel.changeTeamLeader(params)
                25 -> teamModel.updateTeam(params)
                26 -> runBlocking { serviceModel.manageLocationShareStateTeamWide(params) }
                28 -> runBlocking { serviceModel.manageIndividualLocationShareChange() }
                29 -> teamModel.parseTeamLocUpdate(params)

                40 -> pointModel.parsePointCreation(params)
                41 -> pointModel.parsePointCreationResponse(params)
                42 -> pointModel.parseDeletePoint(params)
                44 -> pointModel.handleSync(params)

                60 -> chatModel.parseChatCreation(params)
                61 -> chatModel.parseChatDeletion(params)
                62 -> {}//no need to implement
                63 -> {}//no need to implement
                64 -> chatModel.parseMessage(params)
                65 -> chatModel.parseMultipleMessages(params)
            }
        } catch (e: Exception){
            Log.d("ConnectionService",e.message.toString())
            e.printStackTrace()
        }
    }

    /**
     * Method for handling incoming frames from server. This is first method in "router" cascade of methods.
     * This method invokes method that parses incoming frame based on its type.
     * @param frame [Frame] received from server that will be parsed
     */
    private fun handleFrame(frame: Frame)
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
                Log.d("Connection service","Got an unknown frame, current count is $unknownFrameCounter")
            }
        }
    }

    /**
     * Extension method for managing websocket connection. It handles initial "handshake" with server and syncing with it.
     * After that it parses all incoming frames and when connection is terminated it sets all attributes to correct values
     * to indicate that there is no more connection.
     * @param calledFromOnStart Flag that indicates if this is call is the first call to this method
     */
    private suspend fun DefaultWebSocketSession.manageConnection(calledFromOnStart: Boolean)
    {
        Log.d("Connection service", "Initiating connection sequence")
        connectToServer() //initial handshake
        Log.d(
            "Connection service",
            "Sequence completed... Connection state is: ${serviceModel.connectionState.name} "
        )
        if (serviceModel.connectionState != ConnectionState.CONNECTED) {
            if(calledFromOnStart) stopSelf() //if handshake failed then stop service will not accept any other message
            return
        }
        serviceModel.errorString = ""

        //second+ call to this method needs to reset all model because if some profiles were deleted then no message is sent
        //only profiles that are on server, same goes for teams...
        if (!calledFromOnStart) {
            serviceModel.clearUsers()
            teamModel.clearTeams()

            teamModel.teamLocationUpdateHandlers.forEach {
                it.startSharingLocation()
            }
            chatModel.fetchNewestMessages()
        }
        pointModel.sendSync() //sync local point database and server points database

        if (serviceModel.sharingLocation) { //if location should be shared then start sharing it
            Log.d("Connection Service", "Starting to share location after sending sync")
            if (sharingLocationJobHandler == null) {
                Log.d("Connection Service", "sharing handler was null")
                sharingLocationJobHandler =
                    PeriodicPositionUpdater(serviceModel.period, this@ConnectionService, session, true)
                sharingLocationJobHandler!!.startSharingLocation(serviceModel.period)

            } else {
                Log.d("Connection Service", "sharing handler was null")
                sharingLocationJobHandler?.session = this
                sharingLocationJobHandler?.startSharingLocation(serviceModel.period)
            }

        }
        for (frame in incoming) {

            // disconnection
            if (!connect) {
                Log.d("Connection service", "User initiated disconnect... Closing websocket")
                send(Frame.Text(ClientMessage.bye("User disconnected")))
                send(Frame.Close(CloseReason(CloseReason.Codes.NORMAL, "User disconnected")))
                serviceModel.connectionState = ConnectionState.NOT_CONNECTED
                serviceModel.isConnected = false
                connect = false
                Log.d(
                    "Connection service",
                    "Websocket is closed. Connection state is: ${serviceModel.connectionState.name}"
                )
                break
            }
            Log.d("Connection service", "Parsing incoming frame")
            handleFrame(frame)
            Log.d("Connection service", "Parsed frame")
        }

        //sometimes client ignores ping frame and server will experience ping exception and will close connection even tough
        //client is running, if client is running and this event occur TRY_AGAIN code will be sent from server and client
        //then knows what happened and will reconnect right away
        if(closeReason.await()?.code == CloseReason.Codes.TRY_AGAIN_LATER.code) {
            connectAndRun(false)
        } else {
            Log.d("Connection Service", "Out of the incoming loop")
            if (serviceModel.connectionState != ConnectionState.ERROR) serviceModel.connectionState =
                ConnectionState.NOT_CONNECTED
            serviceModel.isConnected = false
            session.cancel()
            getSharedPreferences("jmb_bms_Server_Info", Context.MODE_PRIVATE).edit {
                putBoolean("connected", false)
                commit()
            }
        }
    }

    /**
     * Method that connects to server and parses all incoming messages. After it connects it sets [session] attribute
     * for other methods and classes to use to send messages to server. If exception occurs it will set all attributes
     * to indicate that something like this happened. Note that this method should be only invoked on another thread.
     * Also note that client won't check servers certificate because it is impossible to get normal certificate
     * on my personal computer while testing. Right now it is not problem because most of the connections are directly to known IP
     * address. In the future I will add option to settings to check/not check validity of certificates.
     */
    private fun connectAndRun(calledFromOnStart: Boolean)
    {
        try {
            val trustAllCerts = object : X509TrustManager {
                override fun checkClientTrusted(chain: Array<out X509Certificate>?, authType: String?) {}
                override fun checkServerTrusted(chain: Array<out X509Certificate>?, authType: String?) {}
                override fun getAcceptedIssuers(): Array<X509Certificate> = emptyArray()
            }
            Log.d("Connection Service","Connect and function was called from " + if(calledFromOnStart) "OnStartCommand" else "Restart" )
            client = HttpClient(CIO){
                engine{
                    https {
                        if(testing)
                        {
                            trustManager = trustAllCerts
                        }
                    }
                }
                install(WebSockets)
            }
            runBlocking {
                try {

                    Log.d("Service data", "${serviceModel.host} ${serviceModel.port} ${this@ConnectionService}")
                    client.wss(
                        method = HttpMethod.Get,
                        host = serviceModel.host,
                        port = serviceModel.port,
                        path = "/connect"
                    ) {
                        session = this@wss
                        manageConnection(calledFromOnStart)
                    }
                } catch (e: Exception) {
                    //when server says error during init phase service will stop but when there will be exception service will survive
                    Log.d(
                        "Connection service",
                        "Experienced exception!\nMessage: ${e.message}\n${e.stackTraceToString()}\nStopping location share"
                    )
                    sharingLocationJobHandler?.stopSharingLoc()
                    teamModel.teamLocationUpdateHandlers.forEach {
                        it.stopSharingLoc()
                    }
                    serviceModel.sharingLocation = false


                    Log.d("Connection service", "Setting error state and sending to service state callback")
                    serviceModel.errorString = e.message ?: "Encountered error"
                    serviceModel.connectionState = ConnectionState.ERROR
                    serviceModel.isConnected = false
                    if (this@ConnectionService::session.isInitialized) session.cancel()

                }
            }
        }catch (e:Exception)
        {
            Log.d(
                "Connection service",
                "Experienced exception!\nMessage: ${e.message}\n${e.stackTraceToString()}\nStopping location share"
            )
            serviceModel.errorString = e.message ?: "Encountered error"
            serviceModel.connectionState = ConnectionState.ERROR
            serviceModel.isConnected = false
        }

    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        val caller = intent?.getStringExtra("Caller")
        //serviceModel = ConnectionDataAndState(this,comCentral)

        val shPref = this.getSharedPreferences("jmb_bms_Server_Info", Context.MODE_PRIVATE)

        //check if intent has host and port, if not use shared preferences
        serviceModel.port = intent?.getIntExtra("Port",0) ?: 0
        serviceModel.host = intent?.getStringExtra("Host") ?: shPref.getString("ServerInfo_IP","") ?: ""

        if(serviceModel.port == 0) serviceModel.port = shPref.getString("ServerInfo_Port","0")?.toInt() ?: 0

        if(intent == null)
        {
            serviceModel.sharingLocation = shPref.getBoolean("Server_LocSh",false)
        }

        //check if service doesn't have permission, but somehow it still started, stop it
        if(doesNotHavePermission)
        {
            Log.d("Connection Service","Can not run as service so I'm not starting")
            serviceModel.connectionState = ConnectionState.ERROR
            stopSelf()

        //if port and host are not set there is no point in connecting
        } else if( serviceModel.port != 0 && serviceModel.host != "") {

            Log.d("Service start", "Service was started by $caller")

            //connect and run must run on its own thread
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
            /*getSharedPreferences("jmb_bms_Server_Info", Context.MODE_PRIVATE).edit {
                putBoolean("connected",true)
                commit()
            }

             */
        }
        return START_STICKY //can be stopped on by calling .stopService or stopSelf
    }

    /**
     * Method used to reconnect with server when service is running but is not connected to server. If session is alive active
     * nothing will happen. Also, if it is going to reconnect resets [serviceModel] into default state.
     */
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
            getSharedPreferences("jmb_bms_Server_Info", Context.MODE_PRIVATE).edit {
                putBoolean("connected",true)
                commit()
            }
        }
    }

    /**
     * Method used to start sharing location to other users. If session is not active nothing will happen.
     * @param period Time between location updates
     * @return true if location sharing started else false
     */
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

    fun disconnect()
    {
        connect = false
    }

    /**
     * Method used to stop location sharing with server.
     */
    suspend fun stopSharingLocation(){
        //Log.d("HERE3","HERE3")
        sharingLocationJobHandler?.stopSharingLoc()
        //Log.d("HERE4","HERE4")
        serviceModel.sharingLocation = false

       // Log.d("HERE5","HERE5")
        if(this::session.isInitialized && serviceModel.connectionState == ConnectionState.CONNECTED)  session.send(Frame.Text(ClientMessage.stopUpdating()))
       // Log.d("HERE6","HERE6")

    }

    /**
     * Method used to change time between location updates
     * @param newDelay New value
     */
    fun changeDelayForLocSh( newDelay: Long){
        serviceModel.period = newDelay
        sharingLocationJobHandler?.changeDelay(newDelay)
    }

    /**
     * Method used to send [Frame.Text] containing [jsonString] to server
     * @param jsonString JSON string with message to server
     */
    fun sendMessage(jsonString: String)
    {
        runOnThread {
            try {
                session.send(Frame.Text(jsonString))
            } catch (e: Exception)
            {
                e.printStackTrace()
            }
        }
    }
    fun createTeamMessage(teamName: String, teamIcon: String, topTeamId: String)
    {
        sendMessage(ClientMessage.createTeam(teamName, teamIcon, topTeamId))
    }

    /**
     * Method that sends delete team message to server (opCode 22)
     * @param teamId ID of the team that will be deleted
     */
    fun deleteTeamMessage(teamId: String)
    {
        sendMessage(ClientMessage.deleteTeam(teamId))
    }

    /**
     * Method that sends add or delete message to server to add/remove user from a team (opCode 23)
     * @param teamId ID of a team where will be user added or removed
     * @param userId ID of user that will be added to team or removed from it
     * @param add Flag indicating if user with [userId] will be added to team, or removed from it, with ID [teamId]
     */
    fun addOrDelUserMessage(teamId: String, userId: String, add: Boolean)
    {
        sendMessage(ClientMessage.addOrDelUser(teamId, userId, add))
    }

    /**
     * Method that sends change team leader message to server (opCode 24)
     * @param teamId ID of them where will be team leader changed
     * @param newLeaderId ID of user that will be new leader
     */
    fun changeTeamLeaderMessage(teamId: String, newLeaderId: String)
    {
        sendMessage(ClientMessage.changeTeamLeader(teamId, newLeaderId))
    }

    /**
     * Method that sends team update message (opCode 25)
     * @param teamId Updated team ID
     * @param newName
     * @param newIcon
     */
    fun teamUpdateMessage(teamId: String, newName: String, newIcon: String)
    {
        sendMessage(ClientMessage.updateTeam(teamId,newName,newIcon))
    }

    /**
     * Method that sends team location sharing state update (opCode 26)
     * @param teamId ID of team where change happens
     * @param on Flag indicating if all users will turn on/off location sharing
     */
    fun handleTeamLocShState(teamId: String,on: Boolean)
    {
        sendMessage(ClientMessage.teamLocShSate(teamId, on))
    }

    /**
     * Method that sends toggle users location sharing (opcode 27)
     * @param teamId id of team in which user is
     * @param userId ID of user whose location sharing will be toggled
     */
    fun handleTeamMemberLocationSharingReq(teamId: String,userId: String)
    {
        sendMessage(ClientMessage.userLocToggle(teamId, userId))
    }

    /**
     * Method that sends point to server (opCode 40)
     * @param id points ID
     */
    fun sendPoint(id: Long)
    {
        CoroutineScope(Dispatchers.IO).launch {
            pointModel.sendPoint(id)
        }
    }

    /**
     * Method that sends delete point message (opCode 42)
     */
    fun deletePoint(id: String?)
    {
        if(id.isNullOrEmpty()) return
        sendMessage(ClientMessage.deletePoint(id))
    }

    /**
     * Method that manually syncs points (opCode 44)
     */
    fun manuallySyncPoints()
    {
        CoroutineScope(Dispatchers.IO).launch{
            pointModel.sendSync()
        }
    }

    /**
     * Method that creates chat room (opCode 60)
     * @param name room name
     * @param members [List] of user ids
     */
    fun createChatRoom(name: String, members: List<String>)
    {
        sendMessage(ClientMessage.createChatRoom(name, members))
    }

    /**
     * Method that sends message to chat (opCode 64)
     * @param chatMessage Message
     */
    suspend fun sendChatMessage(chatMessage: ChatMessage)
    {
        chatModel.sendMessage(chatMessage)
    }

    /**
     * Method that sends fetch request (opCode 65)
     * @param cap oldest message ID
     * @param chatId ID of chat room
     */
    fun sendFetchMessages(cap: Long, chatId: String)
    {
       sendMessage(ClientMessage.fetchMessages(cap, chatId))
    }

    fun deleteChatRoom(id: String)
    {
        sendMessage(ClientMessage.deleteChatRoom(id))
    }

    /**
     * Method that runs code on dedicated thread
     * @param code Code that will be run
     */
    private fun runOnThread(code: suspend () -> Unit)
    {
        thread {
            runBlocking{
                code()
            }
        }
    }

    /**
     * Method that gets users id
     * @return Users id
     */
    fun getUserId(): String = serviceModel.profile.serverId


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

        if(netCallback != null)
        {
            conManager?.unregisterNetworkCallback(netCallback!!)
        }
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

        //TODO for now I will delete them for the testing sake but when I will put in database I wont delete them here
        //TODO sometimes this throws concurrent modification exception so check on that

        //pointModel.dbHelper.close()

        val dbHelper = ChatDBHelper(this,null)
        dbHelper.removeAllChatRooms()
        dbHelper.close()
        //chatModel.dbHelper.close()
        try {
            teamModel.clearTeams()
            serviceModel.listOfUsers.forEach { profile ->
                serviceModel.removePoint(profile,this)
            }
            serviceModel.clearUsers()
        } catch (e:Exception)
        {
            Log.d("Connection Service","Exception during removing...")
        }
    }

    override fun onLowMemory() {
        super.onLowMemory()
        System.gc()
        //Log.d("Connection Service","In ONLOWMEMORY!!!!!!!!!!!!!!!!!!!!")
    }
}