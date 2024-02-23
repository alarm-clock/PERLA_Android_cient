package com.example.jmb_bms.activities

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.jmb_bms.PeriodicBackroundPositionUpdater
import com.example.jmb_bms.model.LocationRepo
import com.example.jmb_bms.view.ServerInfoInputScreen
import com.example.jmb_bms.viewModel.LiveLocationFromLoc
import com.example.jmb_bms.viewModel.LiveTime
import com.example.jmb_bms.viewModel.ServerInfoVM

class TeamActivity : AppCompatActivity() {

    private lateinit var service: PeriodicBackroundPositionUpdater
    private var bound: Boolean = false

    private val currentTime by viewModels<LiveTime>()

    private lateinit var locationRepo : LocationRepo

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        locationRepo = LocationRepo(applicationContext)

        val currentLocation by viewModels<LiveLocationFromLoc> {
            LiveLocationFromLoc.create(locationRepo)
        }
        val serverInfoVM by viewModels<ServerInfoVM> {
            ServerInfoVM.create(applicationContext)
        }

        setContent {
            ServerInfoInputScreen(currentTime,currentLocation, serverInfoVM)
        }
    }
}