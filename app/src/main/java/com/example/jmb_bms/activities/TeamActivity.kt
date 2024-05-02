package com.example.jmb_bms.activities

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import com.example.jmb_bms.model.LocationRepo
import com.example.jmb_bms.view.server.ServerService
import com.example.jmb_bms.view.permissionMissing
import com.example.jmb_bms.viewModel.*

class TeamActivity : ComponentActivity() {

    private val currentTime by viewModels<LiveTime>()

    private lateinit var locationRepo : LocationRepo

    private val reqPermissions = arrayOf(
        Manifest.permission.FOREGROUND_SERVICE,
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION
    )

    private fun checkPermissions(): Boolean{
        for( permission in reqPermissions)
        {
            val checkVal = checkCallingOrSelfPermission(permission)
            if( checkVal != PackageManager.PERMISSION_GRANTED) return false
        }
        return true
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        locationRepo = LocationRepo(applicationContext)

        if(checkPermissions())
        {
            val currentLocation by viewModels<LiveLocationFromLoc> {
                LiveLocationFromLoc.create(locationRepo,this)
            }

            setContent {
                ServerService(currentTime,currentLocation,/*serverInfoVM,serverVM*/){
                    Log.d("Back Button Handler","Calling activity finish")
                    finish()
                }
            }
        } else
        {
            setContent{
                permissionMissing()
            }
        }
    }
}