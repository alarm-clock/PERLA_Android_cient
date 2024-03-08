package com.example.jmb_bms.connectionService

import android.content.Context
import android.util.Log
import com.example.jmb_bms.model.LocationRepo
import io.ktor.websocket.*
import kotlinx.coroutines.*

class PeriodicPositionUpdater(private var delay: Long,
                              val ctx: Context, var session: DefaultWebSocketSession,
                              startAtInit: Boolean = false) {

    private var job : Job = Job()

    private val scope = CoroutineScope(Dispatchers.IO + job)
    init {
        if (startAtInit) launchJob()
    }
    private fun launchJob()
    {
        //TODO maybe check if session is still active

        scope.launch {
            Log.d("Periodic Pos Updater","Starting updating job...")
            LocationRepo(ctx,delay,false).getLocUpdates().collect{ update ->
                Log.d("Periodic Pos Updater","Sending location update $update")
                session.send(locationUpdate(update))
            }
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
        job.children.forEach { it.cancel() }
    }
}