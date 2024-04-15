package com.example.jmb_bms.activities

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import com.example.jmb_bms.LocusVersionHolder
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

class MainActivity : ComponentActivity(){ //AppCompatActivity() {


    private val currentTime by viewModels<LiveTime>()

    private lateinit var locationRepo : LocationRepo

    private lateinit var menuItems: MainMenuItems


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val db = PointDBHelper(this,null)
        db.sendAllPointsToLoc(this)
        db.close()

        LocusVersionHolder.checkAndSotreLocVer(this)

        if( LocusVersionHolder.getLv() == null ) return

        locationRepo = LocationRepo(applicationContext)

        val currentLocation by viewModels<LiveLocationFromLoc> {
            LiveLocationFromLoc.create(locationRepo)
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
            LocusUtils.callStartLocusMap(this)
            finish()
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

