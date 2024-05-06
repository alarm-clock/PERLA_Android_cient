/**
 * @file: TeamActivity.kt
 * @author: Jozef Michal Bukas <xbukas00@stud.fit.vutbr.cz,jozefmbukas@gmail.com>
 * Description: File containing TeamActivity class
 */
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

/**
 * Activity for all server connection, team, and location sharing features. Extends ComponentActivity.
 */
class TeamActivity : ComponentActivity() {

    private val currentTime by viewModels<LiveTime>()

    private lateinit var locationRepo : LocationRepo

    //Most crucial permissions without which service could not run and service would literary crash taking this activity down with it
    private val reqPermissions = arrayOf(
        Manifest.permission.FOREGROUND_SERVICE,
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION,
    )

    /**
     * Method for checking if all required permissions were given to app
     *
     * @return false if some permission was not given else true
     */
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
            val currentLocation by viewModels<LiveLocationFromPhone> {
                LiveLocationFromPhone.create(locationRepo,this)
            }

            setContent {
                ServerService(currentTime,currentLocation){
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