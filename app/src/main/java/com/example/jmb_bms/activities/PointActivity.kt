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
import com.example.jmb_bms.viewModel.LiveLocationFromLoc
import com.example.jmb_bms.viewModel.LiveTime
import locus.api.android.utils.IntentHelper
import locus.api.objects.geoData.Point

class PointActivity: ComponentActivity() {

    private lateinit var dbHelper : PointDBHelper

    private val currentTime by viewModels<LiveTime>()

    private var pointRow : PointRow? = null

    private lateinit var locationRepo : LocationRepo

    private val reqPermission = arrayOf(
        Manifest.permission.CAMERA
    )

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

        if(IntentHelper.isIntentPointTools(intent)) {

            val point = IntentHelper.getPointFromIntent(this, intent)

            if (point == null) finish()

            if (checkPermission()) {
                dbHelper = PointDBHelper(this.applicationContext, null)
                dbHelper.sendAllPointsToLoc(this)

                locationRepo = LocationRepo(applicationContext)
                val currentLocation by viewModels<LiveLocationFromLoc> {
                    LiveLocationFromLoc.create(locationRepo)
                }

                if (point?.extraData?.getParameter(1) == "jmb_bms") {

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
                            _PointScreens.DETAIL,
                            null
                        ) {
                            dbHelper.close()
                            finish()
                        }
                    }
                } else {
                    setContent {
                        AllPointScreens(
                            point!!,
                            currentTime,
                            currentLocation,
                            this.activityResultRegistry,
                            dbHelper,
                            _PointScreens.CREATION,
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
        } else if( intent.getStringExtra("caller") != null) {

            val locBundle = intent.getBundleExtra("location")
            dbHelper = PointDBHelper(this.applicationContext, null)
            locationRepo = LocationRepo(applicationContext)
            val currentLocation by viewModels<LiveLocationFromLoc> {
                LiveLocationFromLoc.create(locationRepo)
            }
            val fromLoc = getSharedPreferences("Point_Menu", Context.MODE_PRIVATE).getBoolean("From_Locus",false)
            setContent{
                AllPointScreens(Point(),currentTime,currentLocation,activityResultRegistry,dbHelper,_PointScreens.CREATION_FROM_LOC,locBundle,true, fromLoc){
                    dbHelper.close()
                    finish()
                }
            }
        } else
        {
            this.getSharedPreferences("Point_Menu", Context.MODE_PRIVATE).edit {
                putBoolean("From_Locus",false)
                commit()
            }

            dbHelper = PointDBHelper(this.applicationContext, null)
            locationRepo = LocationRepo(applicationContext)
            val currentLocation by viewModels<LiveLocationFromLoc> {
                LiveLocationFromLoc.create(locationRepo)
            }
            setContent {
                AllPointScreens(Point(),currentTime,currentLocation,activityResultRegistry,dbHelper,_PointScreens.ALL,null,false, false) {
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