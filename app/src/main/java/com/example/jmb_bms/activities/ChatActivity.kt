/**
 * @file: ChatActivity.kt
 * @author: Jozef Michal Bukas <xbukas00@stud.fit.vutbr.cz,jozefmbukas@gmail.com>
 * Description: File containing ChatActivity class
 */
package com.example.jmb_bms.activities

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.core.view.WindowCompat
import com.example.jmb_bms.model.LocationRepo
import com.example.jmb_bms.view.chat.ChatNavigation
import com.example.jmb_bms.viewModel.LiveLocationFromPhone
import com.example.jmb_bms.viewModel.LiveTime

/**
 * Activity which sets chat related screens. It extends ComponentActivity class.
 */
class ChatActivity: ComponentActivity() {

    private val currentTime by viewModels<LiveTime>()

    private lateinit var locationRepo : LocationRepo
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        locationRepo = LocationRepo(applicationContext)

        val currentLocation by viewModels<LiveLocationFromPhone> {
            LiveLocationFromPhone.create(locationRepo, this)
        }

        //window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING)

        //this unsets screen boundaries so when keyboard is shown on screen top bar won't be out of view
        WindowCompat.setDecorFitsSystemWindows(window,false)

        setContent {
            ChatNavigation(currentLocation, currentTime){ finish() }
        }
    }
}