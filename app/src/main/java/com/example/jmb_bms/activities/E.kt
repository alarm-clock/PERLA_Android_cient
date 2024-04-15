package com.example.jmb_bms.activities

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import androidx.activity.ComponentActivity
import com.example.jmb_bms.connectionService.ConnectionService
import com.example.jmb_bms.model.database.points.PointDBHelper
import kotlin.concurrent.thread

class E: ComponentActivity() {

    private var wait = true

    private var service : ConnectionService? = null
    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, serviceBin: IBinder?) {
            val binder = serviceBin as ConnectionService.LocalBinder
            service = binder.getService()
            wait = false
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            service = null
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        thread {
            val intent = Intent(this, ConnectionService::class.java).putExtra("Caller","ServerVM")
            bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)

            Log.d("TEST-TEST!!!","In activity")

            while (wait){}

            val dbHelper = PointDBHelper(this,null)

            val point = dbHelper.getPoint(1)
            point!!.postedToServer = false
            point.serverId = null
            dbHelper.updatePointIdentById(point)

            Log.d("TEST-TEST!!!","After wait")
            service?.sendPoint(point.id)

            dbHelper.close()
            unbindService(serviceConnection)

            finish()
        }
    }

}