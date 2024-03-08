package com.example.jmb_bms.activities

import android.content.ComponentName
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.jmb_bms.LocusVersionHolder
import com.example.jmb_bms.PeriodicBackroundPositionUpdater
import com.example.jmb_bms.model.LocationRepo
import com.example.jmb_bms.model.MainMenuItems
import com.example.jmb_bms.view.mainMenu
import com.example.jmb_bms.viewModel.LiveLocationFromLoc
import com.example.jmb_bms.viewModel.LiveTime
import kotlinx.coroutines.runBlocking

import locus.api.android.objects.LocusVersion
import locus.api.android.utils.IntentHelper
import locus.api.android.utils.LocusUtils
import locus.api.objects.extra.Location

class MainActivity : ComponentActivity(){ //AppCompatActivity() {


    private val currentTime by viewModels<LiveTime>()

    private lateinit var locationRepo : LocationRepo

    private lateinit var menuItems: MainMenuItems


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        LocusVersionHolder.checkAndSotreLocVer(this)

        if( LocusVersionHolder.getLv() == null ) return

        locationRepo = LocationRepo(applicationContext)

        val currentLocation by viewModels<LiveLocationFromLoc> {
            LiveLocationFromLoc.create(locationRepo)
        }
        menuItems = MainMenuItems(getSharedPreferences("jmb_bms_MainMenu", MODE_PRIVATE),this)

        if(IntentHelper.isIntentMainFunction(intent))
        {
            println("|----------------------------- Over Here MOtherFucker 69 -------------------------|")
            IntentHelper.handleIntentMainFunction(this,intent, object : IntentHelper.OnIntentReceived{

                override fun onReceived(lv: LocusVersion, locGps: Location?, locMapCenter: Location?) {

                    setContent {
                        mainMenu(currentTime, currentLocation, menuItems){ finish() }
                    }
                }
                override fun onFailed() {

                }
            })

        }
        else if( IntentHelper.isIntentPointTools(intent))
        {
            setContent {
                mainMenu(currentTime, currentLocation, menuItems){ finish() }
            }
            //val dataset = arrayOf("Chat" , "Orders" , "Team" , "Settings" , "Points Management") utManager(this@MainActivity)

        }
        else if (IntentHelper.isIntentPointsTools(intent))
        {
            val point = IntentHelper.getPointFromIntent(this,intent);
            //runBlocking { service.sendPoint(point!!); }
        }
        else
        {
            LocusUtils.callStartLocusMap(this)
        }

    }


    override fun onStop() {
        super.onStop()
   //     println("TeamScreen onStop: stopping bound is -> $bound")
     //   if(bound){
       //     unbindService(connection)
         //   bound = false
       // }
    }
}

