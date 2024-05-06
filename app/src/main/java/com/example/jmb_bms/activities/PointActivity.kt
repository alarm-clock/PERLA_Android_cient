/**
 * @file: PointActivity.kt
 * @author: Jozef Michal Bukas <xbukas00@stud.fit.vutbr.cz,jozefmbukas@gmail.com>
 * Description: File containing PointActivity class
 */
package com.example.jmb_bms.activities

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.material3.Text
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.sp
import androidx.core.content.edit
import com.example.jmb_bms.model.LocationRepo
import com.example.jmb_bms.model.database.points.PointDBHelper
import com.example.jmb_bms.model.database.points.PointRow
import com.example.jmb_bms.view.point.AllPointScreens
import com.example.jmb_bms.view.point._PointScreens
import com.example.jmb_bms.viewModel.LiveLocationFromPhone
import com.example.jmb_bms.viewModel.LiveTime
import locus.api.android.utils.IntentHelper
import locus.api.objects.geoData.Point

/**
 *  Activity for all point related features implemented by application. Extends ComponentActivity class.
 */
class PointActivity: ComponentActivity() {

    private lateinit var dbHelper : PointDBHelper

    private val currentTime by viewModels<LiveTime>()

    private var pointRow : PointRow? = null

    private lateinit var locationRepo : LocationRepo

    //permission for camera use in point creation/update
    private val reqPermission = arrayOf(
        Manifest.permission.CAMERA
    )

    /**
     * Method for checking if all required permissions were given to app
     *
     * @return false if some permission was not given else true
     */
    private fun checkPermission(): Boolean{

        for( permission in reqPermission)
        {
            val check = checkCallingOrSelfPermission(permission)
            if(check != PackageManager.PERMISSION_GRANTED) return false
        }
        return true
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //called directly from locus
        if(IntentHelper.isIntentPointTools(intent)) {

            val point = IntentHelper.getPointFromIntent(this, intent)

            if (point == null) finish()

            if (checkPermission()) {

                dbHelper = PointDBHelper(this.applicationContext, null)
                dbHelper.sendAllPointsToLoc(this)

                locationRepo = LocationRepo(applicationContext)
                val currentLocation by viewModels<LiveLocationFromPhone> {
                    LiveLocationFromPhone.create(locationRepo,this)
                }
                //check if point has application tag as extra stored in it, if yes it means it is my point
                if (point?.extraData?.getParameter(1) == "jmb_bms") {

                    //because this activity uses GetLocFromLocActivity it destroys is navigational graph, and it must be simulated
                    //so here I store from where this activity was called so that when simulating road back I know what is target
                    this.getSharedPreferences("Point_Menu", Context.MODE_PRIVATE).edit {
                        putBoolean("From_Locus",true)
                        commit()
                    }

                    setContent {
                        AllPointScreens(
                            point,
                            currentTime,
                            currentLocation,
                            this.activityResultRegistry,
                            dbHelper,
                            _PointScreens.DETAIL,  //setting point detail screen
                            null
                        ) {
                            dbHelper.close() //on return close connection with db and finish activity
                            finish()
                        }
                    }
                //point does not have my tag so that must mean it is new point
                } else {
                    setContent {
                        AllPointScreens(
                            point!!,
                            currentTime,
                            currentLocation,
                            this.activityResultRegistry,
                            dbHelper,
                            _PointScreens.CREATION,  //set point creation
                            null
                        ) {
                            dbHelper.close()
                            finish()
                        }
                    }
                }
            } else {
                setContent {
                    Text("Dont have permission to camera", fontSize = 30.sp, color = Color.Red)
                }
            }
        // call from GetLocFromLocActivity
        } else if( intent.getStringExtra("caller") != null) {

            val locBundle = intent.getBundleExtra("location")
            dbHelper = PointDBHelper(this.applicationContext, null)
            locationRepo = LocationRepo(applicationContext)
            val currentLocation by viewModels<LiveLocationFromPhone> {
                LiveLocationFromPhone.create(locationRepo,this)
            }
            val fromLoc = getSharedPreferences("Point_Menu", Context.MODE_PRIVATE).getBoolean("From_Locus",false)
            setContent{
                AllPointScreens(
                    Point(),
                    currentTime,
                    currentLocation,
                    activityResultRegistry,
                    dbHelper,
                    _PointScreens.CREATION_FROM_LOC, // point creation screen, VM takes new location
                    locBundle,
                    true, //simulated going back
                    fromLoc
                ){
                    dbHelper.close()
                    finish()
                }
            }
        //called from main activity
        } else
        {
            this.getSharedPreferences("Point_Menu", Context.MODE_PRIVATE).edit {
                putBoolean("From_Locus",false)
                commit()
            }

            dbHelper = PointDBHelper(this.applicationContext, null)
            locationRepo = LocationRepo(applicationContext)
            val currentLocation by viewModels<LiveLocationFromPhone> {
                LiveLocationFromPhone.create(locationRepo, this)
            }
            setContent {
                AllPointScreens(
                    Point(),
                    currentTime,
                    currentLocation,
                    activityResultRegistry,
                    dbHelper,
                    _PointScreens.ALL,  //all points menu screen
                    null,
                    false, //no need to simulate
                    false //called from main activity
                ) {
                    dbHelper.close()
                    finish()
                }
            }
        }
    }
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        Log.d("OnSaveInstanceState--------------",pointRow.toString())
        pointRow?.putIntoBundle(outState)

    }

    //check how to handle app termination case
    override fun onDestroy() {
        super.onDestroy()
        dbHelper.close()
    }
}