/**
 * @file: MainActivity.kt
 * @author: Jozef Michal Bukas <xbukas00@stud.fit.vutbr.cz,jozefmbukas@gmail.com>
 * Description: File containing MainActivity class
 */
package com.example.jmb_bms.activities

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import com.example.jmb_bms.LocusVersionHolder
import com.example.jmb_bms.connectionService.ConnectionService
import com.example.jmb_bms.model.LocationRepo
import com.example.jmb_bms.model.menu.MainMenuItems
import com.example.jmb_bms.model.database.points.PointDBHelper
import com.example.jmb_bms.view.mainMenu
import com.example.jmb_bms.viewModel.LiveLocationFromLoc
import com.example.jmb_bms.viewModel.LiveTime

import locus.api.android.objects.LocusVersion
import locus.api.android.utils.IntentHelper
import locus.api.android.utils.LocusUtils
import locus.api.objects.extra.Location


class MainActivity : ComponentActivity(){


    private val currentTime by viewModels<LiveTime>()

    private lateinit var locationRepo : LocationRepo

    private lateinit var menuItems: MainMenuItems

    private var service : ConnectionService? = null

    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, serviceBin: IBinder?) {
            val binder = serviceBin as ConnectionService.LocalBinder
            service = binder.getService()
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            service = null
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        bind()

        val db = PointDBHelper(this,null)
        db.sendAllPointsToLoc(this)
        db.close()

        LocusVersionHolder.checkAndSotreLocVer(this)

        if( LocusVersionHolder.getLv() == null ) return

        locationRepo = LocationRepo(applicationContext)

        val currentLocation by viewModels<LiveLocationFromLoc> {
            LiveLocationFromLoc.create(locationRepo, this)
        }
        menuItems = MainMenuItems(getSharedPreferences("jmb_bms_MainMenu", MODE_PRIVATE),this)

        if(IntentHelper.isIntentMainFunction(intent))
        {
            IntentHelper.handleIntentMainFunction(this,intent, object : IntentHelper.OnIntentReceived{

                override fun onReceived(lv: LocusVersion, locGps: Location?, locMapCenter: Location?) {

                }
                override fun onFailed() {

                }
            })
        }
        else if( IntentHelper.isIntentPointTools(intent))
        {
            val point = IntentHelper.getPointFromIntent(this,intent);
        }
        else if (IntentHelper.isIntentPointsTools(intent))
        {
            finish()
        }
        else
        {
            setContent {
                mainMenu(currentTime, currentLocation, menuItems){ finish() }
            }
            //finish()
        }
    }

    fun bind()
    {
        if(service != null) return

        val running = this.getSharedPreferences("jmb_bms_Server_Info", Context.MODE_PRIVATE).getBoolean("Service_Running",false)

        if(!running) return

        Log.d("MainActivity", "Binding to service")
        val intent = Intent(this, ConnectionService::class.java).putExtra("Caller","CreatePoint")
        //startForegroundService(intent)
        bindService(intent, serviceConnection, 0)
    }

    fun unbind()
    {
        if( service != null)
        {
            unbindService(serviceConnection)
            //stopService(Intent(this, ConnectionService::class.java))
        }
        service = null
    }


    override fun onStop() {
        super.onStop()
   //     println("TeamScreen onStop: stopping bound is -> $bound")
        unbind()
    }

    override fun onRestart() {
        super.onRestart()
        bind()
    }
}

