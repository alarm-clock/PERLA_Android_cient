package com.example.jmb_bms

import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import androidx.core.content.edit
import androidx.work.Worker
import androidx.work.WorkerParameters
import locus.api.android.ActionBasics
import locus.api.android.features.periodicUpdates.UpdateContainer
import locus.api.android.objects.LocusVersion
import locus.api.android.utils.LocusUtils
import kotlin.concurrent.thread

class PeriodicBackroundPositionUpdater() : Service() {

    var run : Boolean = true
    private var thread: Thread? = null


    private fun handleUpdate(lv : LocusVersion?, uc: UpdateContainer?)
    {
        thread = Thread{

            while(run){
                Thread.sleep(5000)
                println("handling upadte")

            }
        }
        thread?.start()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        handleUpdate(null,null)

        return START_NOT_STICKY
    }

    override fun onDestroy() {

        if( thread != null) {
            println("ending")
            run = false
        }
    }

    override fun onBind(p0: Intent?): IBinder? {
        TODO("Not yet implemented")
        return null
    }
}