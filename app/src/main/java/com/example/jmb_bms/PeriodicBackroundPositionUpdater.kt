package com.example.jmb_bms

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent


import android.os.Binder

import android.os.IBinder

import androidx.core.app.NotificationCompat

import com.google.gson.Gson

import io.ktor.client.*
import io.ktor.client.plugins.websocket.*
import io.ktor.http.*
import io.ktor.utils.io.*
import io.ktor.websocket.*
import kotlinx.coroutines.isActive
import kotlinx.coroutines.runBlocking
import locus.api.android.ActionBasics
import locus.api.android.ActionDisplayPoints
import locus.api.android.features.periodicUpdates.UpdateContainer
import locus.api.android.objects.LocusVersion
import locus.api.android.objects.PackPoints
import locus.api.android.utils.LocusUtils
import locus.api.objects.geoData.Point
import java.net.ConnectException
import java.nio.channels.UnresolvedAddressException
import kotlin.concurrent.thread

class PeriodicBackroundPositionUpdater() : Service() {

    var run : Boolean = true
    var connect: Boolean =true
    private var thread: Thread? = null
    private var delay: Long = 5000
    private var shareLoc: Boolean = false
    lateinit var client: HttpClient
    private val IdList = ArrayList<String>()
    lateinit var session: DefaultWebSocketSession
    private val binder = LocalBinder()

    private var cnt = 0


    inner class LocalBinder : Binder(){
        fun getService(): PeriodicBackroundPositionUpdater = this@PeriodicBackroundPositionUpdater
    }


    override fun onCreate() {
        super.onCreate()
        val channel = NotificationChannel("jmb_bms_1","jmb_bms notification channel" , NotificationManager.IMPORTANCE_DEFAULT)
        val notificatioManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificatioManager.createNotificationChannel(channel)
        val notification = NotificationCompat.Builder(this,"jmb_bms_1").setContentTitle("Connecting to server...").build()

        startForeground(1,notification)
    }
    private fun shareLocationWithServer()
    {
        this.thread = thread {
            runBlocking {
                while (run) {
                    println("thread is running")
                    Thread.sleep(delay)
                    if(!run)break
                    LocusUtils.getActiveVersion(this@PeriodicBackroundPositionUpdater)?.let { lv ->
                        ActionBasics.getUpdateContainer(this@PeriodicBackroundPositionUpdater, lv)?.let { uc ->
                            handleUpdate(lv, uc)
                        } ?: run {
                            handleUpdate(lv, null)
                        }
                    } ?: run {
                        handleUpdate(null, null)
                    }
                }
                try {
                    session.send("loc|stop")
                } catch (e: Exception)
                {
                    println(e.message)
                }

            }
        }
        //this.thread?.start()
    }

    private suspend fun handleUpdate(lv: LocusVersion?, uc: UpdateContainer?)
    {
        if( lv == null || uc == null)
        {
            println("lv or uc is null stopping location share")
            run = false
        } else
        {
            if(!run || !session.isActive || !connect)return

            try {
                session.send("loc|${uc.locMyLocation.latitude}|${uc.locMyLocation.longitude}")
            } catch (e: CancellationException)
            {
                println("eeeeeeeeeee")
            }

        }
    }
    private fun getNextParamFromMsg(text: String) : String = text.substring(0, if (text.indexOf('|') != -1) text.indexOf('|') else text.length )

    private fun deletePrevoiusParamFromMsg( text: String): String
    {
        return text.replaceRange(0,if(text.indexOf('|') != -1) text.indexOf('|') + 1 else text.length , "")
    }
    private fun getIdFromTxt(text: String) = getNextParamFromMsg(text.replaceRange(0,text.indexOf('|') + 1,""))
    private fun createPointForLocusAndSendIt(id: String, lat: Double, long: Double)
    {
        println("createPointForLocusAndSendIt: started with arguments -> id $id lat $lat long $long")
        val location = locus.api.objects.extra.Location()
        location.latitude = lat
        location.longitude = long
        println("createPointForLocusAndSendIt: creating point with name -> user_${id}_location")
        var point = Point("user_${id}_location",location)
        println("user_${id}_location: creating packPoints object with name -> user_${id}_location")
        var packPoints = PackPoints("user_${id}_location")
        packPoints.addPoint(point)
        println("createPointForLocusAndSendIt: sending point with sendPackSilent function")
        ActionDisplayPoints.sendPackSilent(this, packPoints , false)
        println("createPointForLocusAndSendIt: sent point to locus")
    }

    private fun createNewPointAndSendItToLoc(txt: String)
    {
        println("createNewPointAndSendItToLoc: function started with text -> $txt")
        var text = txt
        text = deletePrevoiusParamFromMsg(text)
        println("createNewPointAndSendItToLoc: function changed text to -> $text")
        val id: String = getNextParamFromMsg(text)
        println("createNewPointAndSendItToLoc: extracted id from text -> $id")
        text = deletePrevoiusParamFromMsg(text)
        println("createNewPointAndSendItToLoc: function changed text to -> $text")
        val lat : Double = getNextParamFromMsg(text).toDouble()
        println("createNewPointAndSendItToLoc: extracted latitude -> $lat")
        text = deletePrevoiusParamFromMsg(text)
        println("createNewPointAndSendItToLoc: function changed text to -> $text")
        val long : Double = getNextParamFromMsg(text).toDouble()
        println("createNewPointAndSendItToLoc: extracted longitude -> $long")
        createPointForLocusAndSendIt(id,lat,long)
        println("createNewPointAndSendItToLoc: adding id $id to list of ids")
        IdList.add(id)
    }
    private fun getLocFromTxt(txt: String) : locus.api.objects.extra.Location
    {
        println("getLocFromTxt: function started with text -> $txt")
        var text = txt

        text = deletePrevoiusParamFromMsg(text)
        println("getLocFromTxt: changed text to -> $text")

        text = deletePrevoiusParamFromMsg(text)
        println("getLocFromTxt: changed text to -> $text")

        val lat = getNextParamFromMsg(text).toDouble()
        println("getLocFromTxt: extracted latitude from text -> $lat")

        text = deletePrevoiusParamFromMsg(text)
        println("getLocFromTxt: changed text to -> $text")

        val long = getNextParamFromMsg(text).toDouble()
        println("getLocFromTxt: extracted longitude from text -> $long")

        return locus.api.objects.extra.Location(lat,long)
    }

    private fun parseLocUpdate(text: String)
    {
        println("parseLocUpdate: getting point ids...")
        val ids = ActionBasics.getPointsId(this@PeriodicBackroundPositionUpdater,LocusVersionHolder.getLvNotNull(),"user_${getIdFromTxt(text)}_location")

        println("parseLocUpdate: recieved point ids from locus is -> ${if (ids.isEmpty()) "empty" else "has something"}")
        if( ids.isNotEmpty() )
        {
            println("parseLocUpdate: getting point from locus with id -> ${ids[0]}")
            val point = ActionBasics.getPoint(this@PeriodicBackroundPositionUpdater,LocusVersionHolder.getLvNotNull(),ids[0]) ?: return

            println("parseLocUpdate: got point with name ${point.name}  with id ${point.id}")
            point.location = getLocFromTxt(text)

            println("parseLocUpdate: changed points location to -> lat ${point.location.latitude}    long ${point.location.longitude}")

            ActionBasics.updatePoint(this@PeriodicBackroundPositionUpdater,LocusVersionHolder.getLvNotNull(),point,true)
            println("parseLocUpdate: updated point in locus map")

        } else createNewPointAndSendItToLoc(text)
    }

    private fun addPoint(text: String)
    {

        val point = text.substring(2,text.length)
        val pointObj: Point = Gson().fromJson(point,Point::class.java);

        var packPoints = PackPoints("user_${cnt}_location")
        packPoints.addPoint(pointObj)
        println("createPointForLocusAndSendIt: sending point with sendPackSilent function")
        ActionDisplayPoints.sendPackSilent(this, packPoints , false)
        cnt++
    }

    private fun connectAndRun(host: String , port : Int)
    {
        client = HttpClient{
            install(WebSockets)
        }
        runBlocking {
            println("connectAndRun: starting websocket...")
            try {
                client.webSocket(method = HttpMethod.Get, host = host, port = port, path = "/connect") {
                    session = this@webSocket

                    println("connectAndRun in websocket: sending hy to server")
                    send(Frame.Text("Hy"))
                    val fram = incoming.receive()
                    when (fram) {
                        is Frame.Text -> {
                            val text = fram.readText()
                            println("connectAndRun in websocket: received $text from server")
                        }

                        else -> {
                            println("connectAndRun in websocket: recieved frame which isn't text -> sending Frame.Close()")
                            send(Frame.Close())
                            Errors.setLastConErr(ConnectionErr.UNKNOWN_SERVER_RESPONSE)
                            Errors.latch.countDown()
                        }

                    }
                    Errors.latch.countDown()
                    for (frame in incoming) {
                        if (!connect) {
                            println("connectAndRun in websocket: connect was set to false -> disconnecting from server")

                            println("connectAndRun in websocket: sending Frame.Close() to server")
                            //send(Frame.Close())
                            break
                        }
                        when (frame) {
                            is Frame.Text -> {
                                var text = frame.readText()

                                println("connectAndRun in websocket: recieved text from server is $text")

                                if (text[0] == '2') {
                                    println("connectAndRun in websocket: received type 2 message from server -> $text")
                                    parseLocUpdate(text)
                                } else if (text[0] == '1') {
                                    println(
                                        "connectAndRun in websocket: received type 1 message from server -> $text  parsed id is ${
                                            getIdFromTxt(
                                                text
                                            )
                                        }"
                                    )
                                    ActionDisplayPoints.removePackFromLocus(
                                        this@PeriodicBackroundPositionUpdater,
                                        "user_${getIdFromTxt(text)}_location"
                                    )

                                } else if (text[0] == '5')
                                {

                                }
                                else {

                                    println("connectAndRun in websocket: received frame wasn't text -> disconnecting from server \n connectAndRun in websocket: sending Frame.Close() to server")
                                    break
                                }

                            }

                            else -> {
                                println("connectAndRun in websocket: received frame wasn't text -> disconnecting from server \n connectAndRun in websocket: sending Frame.Close() to server")
                                break;
                            }
                        }
                    }
                    println("e")
                    if (connect) send(Frame.Close())
                }
                stopSelf()

            } catch (e: ConnectException)
            {

                println("websocket: encountered Connect exception waiting 5 sec")


                //println("websocket: done waiting")
                Errors.setLastConErr(ConnectionErr.INVALID_HOST)
                Errors.latch.countDown()

            } catch (e: UnresolvedAddressException)
            {

                println("websocket: encountered Unresolved Address exception waiting 5 sec")

                // println("websocket: done waiting")

                Errors.setLastConErr(ConnectionErr.INVALID_HOST)
                Errors.latch.countDown()

            } catch (e : Exception)
            {
                Errors.setLastConErr(ConnectionErr.COULD_NOT_CONNECT)
                println("websocket error: ${e.message}")
                Errors.latch.countDown()
            }
        }
    }
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        val shPref = getSharedPreferences("TeamSCSharedPref", MODE_PRIVATE)
        val host = shPref.getString("IPAddr" , null)
        val port = shPref.getInt("Port" , 0)

        println("onStartCommand: extracted host $host and port $port   from shared preferences")


        val thr = Thread({
            println("onStartCommand: starting thread with websocket")
            if(host != null) connectAndRun(host,port)
        },"connectAndRunThread")
        thr.start()

        return START_NOT_STICKY
    }


    suspend fun sendPoint(point: Point)
    {
        val data = Gson().toJson(point);
        session.send("5|" +  data);
    }
    override fun onDestroy() {

        if( thread != null) {
            println("onDestroy: setting run to false")
            run = false

        }
        println("onDestroy: setting connect to false")

        connect =false

        //when destroing from running apps screen point on the other phone was still visible fix it !!!!!!!!

        if(this::session.isInitialized) {
            runBlocking {

                println("isSession active : ${session.isActive}")
                if(session.isActive)
                {
                    try{

                        session.send("loc|stop")

                        session.close(CloseReason(1000, "Normal closure"))
                    } catch (e: CancellationException)
                    {
                        println(e.message)
                        println(e.cause)
                    }
                }
                println("isSession active : ${session.isActive}")
            }
        }

        IdList.forEach{
            println("connectAndRun in websocket: removing point for user with id $it ")
            ActionDisplayPoints.removePackFromLocus(this@PeriodicBackroundPositionUpdater,"user_${it}_location")
        }

    }

    override fun onBind(p0: Intent?): IBinder {
        return binder
    }

    fun startSharingLocation()
    {
        println("startSharingLocation: set run to true")
        run = true
        shareLocationWithServer()
    }

    fun stopSharingLocationWithServer()
    {
        println("stopSharingLocation: run set to false")
        run = false
    }
}