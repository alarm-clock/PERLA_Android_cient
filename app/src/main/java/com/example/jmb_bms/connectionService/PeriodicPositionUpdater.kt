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

class PeriodicPositionUpdater(private var delay: Long,
                              val ctx: Context, var session: DefaultWebSocketSession,
                              startAtInit: Boolean = false,val teamId: String? = null,val userLoc: Boolean = true) {

    private var job : Job = Job()

    private var run = true

    private val scope = CoroutineScope(Dispatchers.IO + job)
    init {
        if (startAtInit) launchJob()
    }
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
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
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

                //val loc = task.await()
            }
            /*
            LocationRepo(ctx,delay,false).getLocUpdates().collect{ update ->
                Log.d("Periodic Pos Updater","Sending location update $update")
                if(userLoc)  session.send(ClientMessage.locationUpdate(update))
                else session.send(ClientMessage.teamLocationUpdate(teamId!!,update))
            }

             */
        }
    }
    fun changeDelay(delay: Long)
    {
        if(this.delay == delay) return

        if(!job.children.none()) {
            stopSharingLoc()
            this.delay = delay
            launchJob()
        }
    }
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
    fun stopSharingLoc()
    {
        run = false
        job.children.forEach { it.cancel() }
    }
}