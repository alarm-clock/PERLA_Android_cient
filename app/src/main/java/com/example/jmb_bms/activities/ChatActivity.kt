package com.example.jmb_bms.activities

import android.os.Bundle
import android.os.PersistableBundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.core.view.WindowCompat
import com.example.jmb_bms.model.LocationRepo
import com.example.jmb_bms.view.chat.ChatNavigation
import com.example.jmb_bms.viewModel.LiveLocationFromLoc
import com.example.jmb_bms.viewModel.LiveTime

class ChatActivity: ComponentActivity() {

    private val currentTime by viewModels<LiveTime>()

    private lateinit var locationRepo : LocationRepo
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        locationRepo = LocationRepo(applicationContext)

        val currentLocation by viewModels<LiveLocationFromLoc> {
            LiveLocationFromLoc.create(locationRepo)
        }

        //window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING)

        WindowCompat.setDecorFitsSystemWindows(window,false)

        setContent {
            ChatNavigation(currentLocation, currentTime){ finish() }
        }
    }
}