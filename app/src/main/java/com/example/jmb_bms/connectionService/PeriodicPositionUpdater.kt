/**
 * @file: PeriodicPositionUpdater.kt
 * @author: Jozef Michal Bukas <xbukas00@stud.fit.vutbr.cz,jozefmbukas@gmail.com>
 * Description: File containing PeriodicPositionUpdater class
 */
package com.example.jmb_bms.connectionService

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.LocationManager
import android.location.LocationProvider
import android.util.Log
import androidx.core.app.ActivityCompat
import com.example.jmb_bms.model.LocationRepo
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import io.ktor.websocket.*
import kotlinx.coroutines.*

/**
 * Class that periodically on IO coroutine scope retrieves users location and sends it to server.
 * @param delay Period of one retrieval cycle
 * @param ctx Context used to get FusedLocationProviderClient instance
 * @param session Active websocket session where location updates will be sent
 * @param startAtInit Flag that indicates if location updating should start right away
 * @param teamId teamID used to tag location updated if location update is sent on teams behalf
 * @param userLoc Flag indicating if location is sent on clients behalf of teams behalf
 * @constructor starts location sharing job if [startAtInit] flag is set to true
 */
class PeriodicPositionUpdater(private var delay: Long,
                              val ctx: Context, var session: DefaultWebSocketSession,
                              startAtInit: Boolean = false,val teamId: String? = null,val userLoc: Boolean = true) {

    private var job : Job = Job()

    private var run = true

    private val scope = CoroutineScope(Dispatchers.IO + job)
    init {
        if (startAtInit) launchJob()
    }

    /**
     *  Method that launches location sharing job that will send location updates to server in [delay] long cycles
     */
    private fun launchJob()
    {
        //TODO maybe check if session is still active
        if(!userLoc && teamId == null) return
        run = true

        scope.launch {


            val ser = LocationServices.getFusedLocationProviderClient(ctx)

            if (ActivityCompat.checkSelfPermission(
                    ctx,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                    ctx,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return@launch
            }

            while (run)
            {
                delay(delay)
                //val task = async { ser.lastLocation}

                ser.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY,null).addOnSuccessListener {
                    runBlocking {
                        try {
                            if(userLoc)  session.send(ClientMessage.locationUpdate("${it.latitude} - ${it.longitude}"))
                            else session.send(ClientMessage.teamLocationUpdate(teamId!!,"${it.latitude} - ${it.longitude}"))
                        } catch (e: Exception)
                        {
                            Log.d("Periodic position updater","experienced exception while sending location ${e.message}")
                        }
                    }
                }
            }
        }
    }

    /**
     * Method that stops updates [delay] attribute to [delay].
     * @param delay New delay
     */
    fun changeDelay(delay: Long)
    {
        if(this.delay == delay) return

        if(!job.children.none()) {
            stopSharingLoc()
            this.delay = delay
            launchJob()
        }
    }

    /**
     * Method that starts location sharing if no location sharing job is running.
     * @param delay new delay. Leave empty to use same delay as before
     */
    fun startSharingLocation(delay: Long = this.delay)
    {
        Log.d("Periodic Pos Updater","In start sharing location... Is job active? ${job.isActive}")
        if(!job.children.none())
        {
            Log.d("Periodic Pos Updater","This does not work")
            return
        }
        this.delay = delay
        launchJob()
    }

    /**
     * Method that stops location sharing job
     */
    fun stopSharingLoc()
    {
        run = false
        job.children.forEach { it.cancel() }
    }
}