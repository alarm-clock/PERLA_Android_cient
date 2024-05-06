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
import com.example.jmb_bms.viewModel.LiveLocationFromPhone
import com.example.jmb_bms.viewModel.LiveTime

import locus.api.android.objects.LocusVersion
import locus.api.android.utils.IntentHelper
import locus.api.objects.extra.Location


/**
 * MainActivity
 *
 * This class hosts main activity of whole application. In short, it is main menu of  whole app.
 * It extends ComponentActivity
 */
class MainActivity : ComponentActivity(){


    private val currentTime by viewModels<LiveTime>()

    private lateinit var locationRepo : LocationRepo

    private lateinit var menuItems: MainMenuItems

    private var service : ConnectionService? = null

    private val serviceConnection = object : ServiceConnection {

        /**
         * Method which is called when activity connects to service
         *
         * @param name Specifier of application component. Not used
         * @param serviceBin Service binder
         */
        override fun onServiceConnected(name: ComponentName?, serviceBin: IBinder?) {
            val binder = serviceBin as ConnectionService.LocalBinder
            service = binder.getService()
        }

        /**
         * Method which is called when activity disconnects from service
         *
         * @param name Specifier of application component. Not used
         */
        override fun onServiceDisconnected(name: ComponentName?) {
            service = null
        }
    }


    /**
     *  Method called when activity created. Part of its life cycle. It prepares db helper and viewModels for time and location.
     *  Then sets screen content.
     *
     *  @param savedInstanceState Previous instance state saved in bundle
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        bind()

        val db = PointDBHelper(this,null)
        db.sendAllPointsToLoc(this)
        db.close()

        LocusVersionHolder.checkAndSotreLocVer(this)

        if( LocusVersionHolder.getLv() == null ) return

        locationRepo = LocationRepo(applicationContext)

        val currentLocation by viewModels<LiveLocationFromPhone> {
            LiveLocationFromPhone.create(locationRepo, this)
        }
        menuItems = MainMenuItems(getSharedPreferences("jmb_bms_MainMenu", MODE_PRIVATE),this)

        if(IntentHelper.isIntentMainFunction(intent))
        {
            IntentHelper.handleIntentMainFunction(this,intent, object : IntentHelper.OnIntentReceived{

                override fun onReceived(lv: LocusVersion, locGps: Location?, locMapCenter: Location?) {
                    setContent {
                        mainMenu(currentTime, currentLocation, menuItems){ finish() }
                    }
                }
                override fun onFailed() {}
            })
        }
        else if( IntentHelper.isIntentPointTools(intent))
        {
            //val point = IntentHelper.getPointFromIntent(this,intent);
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
        }
    }

    /**
     * Function for binding to service if it is running
     */
    fun bind()
    {
        //service is already bound
        if(service != null) return

        val running = this.getSharedPreferences("jmb_bms_Server_Info", Context.MODE_PRIVATE).getBoolean("Service_Running",false)

        if(!running) return

        Log.d("MainActivity", "Binding to service")
        val intent = Intent(this, ConnectionService::class.java).putExtra("Caller","CreatePoint")
        bindService(intent, serviceConnection, 0)
    }

    /**
     * Function for unbinding from service
     */
    fun unbind()
    {
        if( service != null)
        {
            unbindService(serviceConnection)
        }
        service = null
    }


    override fun onStop() {
        super.onStop()
        unbind()
    }

    override fun onRestart() {
        super.onRestart()
        bind()
    }
}

