package com.example.jmb_bms

import android.annotation.SuppressLint
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.widget.Switch
import android.widget.TextView
import androidx.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.edit
import androidx.fragment.app.Fragment
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.example.jmb_bms.data.TeamSCData
import com.example.jmb_bms.data.TeamSCFrgEnum
import java.util.*
import java.util.concurrent.TimeUnit

class TeamScreen : AppCompatActivity() {

    lateinit var teamSCData: TeamSCData
    lateinit var fragFill: TeamSCHostAndPortFill
    lateinit var fragMain: TeamSCMainSC
    lateinit var fragConn: TeamSCConnecting
    private lateinit var service: PeriodicBackroundPositionUpdater
    private var bound: Boolean = false
    private var prevFrg : TeamSCFrgEnum = TeamSCFrgEnum.FRGFILL

    fun getPrevFrg() = prevFrg

    private val connection = object : ServiceConnection{
        override fun onServiceConnected(p0: ComponentName, p1: IBinder) {

            val binder = p1 as PeriodicBackroundPositionUpdater.LocalBinder
            this@TeamScreen.service = binder.getService()
            println("onServiceConnected: setting bound to true")
            bound = true
        }

        override fun onServiceDisconnected(p0: ComponentName?) {
            bound =false
        }
    }

    fun createFragment(frag: Fragment , viewId: Int )
    {
        supportFragmentManager.beginTransaction()
            .add( viewId,frag)
            .commit()
    }
    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val sharedPref = getSharedPreferences("TeamSCSharedPref", MODE_PRIVATE)
        teamSCData = TeamSCData(sharedPref)

        setContentView(R.layout.team_screen_frame)

        fragFill = TeamSCHostAndPortFill()
        fragMain = TeamSCMainSC()
        fragConn = TeamSCConnecting()

        if(teamSCData.isConnected())
        {
            Intent(this, PeriodicBackroundPositionUpdater::class.java).also {intent ->
                bindService(intent,connection,Context.BIND_AUTO_CREATE)
            }
        }

        if( teamSCData.hasIPAndPort()) createFragment(fragMain, R.id.teamSCFrame2)
        else createFragment(fragFill,R.id.teamSCFrame)

    }

    override fun onStop() {
        super.onStop()
        println("TeamScreen onStop: stopping bound is -> $bound")
        if(bound){
            unbindService(connection)
            bound = false
        }
    }

    fun startSharingLocationWithServer()
    {
        println("TeamScreen startSharingLocationWithServer: starting bound is -> $bound")
        if(bound)
        {
            service.startSharingLocation()
        }
    }
    fun stopSharingLocationWithServer()
    {
        println("TeamScreen stopSharingLocationWithServer: stopping bound is -> $bound")
        if(bound)
        {
            service.stopSharingLocationWithServer()
        }
    }
    fun getCOnnection() = this.connection
    fun getBound() = bound
    fun setBoundToFalse() { bound = false}


    private fun addFragment(newFrag: Fragment)
    {
        when(newFrag)
        {
            is TeamSCHostAndPortFill ->
            {
                supportFragmentManager.beginTransaction()
                    .add(R.id.teamSCFrame, newFrag)
                    .commit()
            }
            is TeamSCMainSC ->
            {
                supportFragmentManager.beginTransaction()
                    .add(R.id.teamSCFrame2,newFrag)
                    .commit()
            }
            is TeamSCConnecting ->
            {
                supportFragmentManager.beginTransaction()
                    .add(R.id.teamSCFrame3,newFrag)
                    .commit()
            }

        }
    }

    fun changeFragments(activeFrag : Fragment , newFrag: Fragment)
    {
        when(activeFrag)
        {
            is TeamSCHostAndPortFill -> {
                supportFragmentManager.beginTransaction()
                    .remove(activeFrag)
                    .commit()
                fragFill = TeamSCHostAndPortFill()
                prevFrg = TeamSCFrgEnum.FRGFILL
            }
            is TeamSCMainSC ->
            {

                supportFragmentManager.beginTransaction()
                    .remove(activeFrag)
                    .commit()
                fragMain = TeamSCMainSC()
                prevFrg = TeamSCFrgEnum.FRGMAIN
            }
            is TeamSCConnecting ->
            {
                supportFragmentManager.beginTransaction()
                    .remove(activeFrag)
                    .commit()
                fragConn = TeamSCConnecting()
                prevFrg = TeamSCFrgEnum.FRGCONN
            }
        }
        addFragment(newFrag)
    }

    fun startServiceAndBind()
    {
        startForegroundService(Intent(this,PeriodicBackroundPositionUpdater::class.java))
        Intent(this,PeriodicBackroundPositionUpdater::class.java).also { intent ->
            this.bindService(intent,getCOnnection(), Context.BIND_AUTO_CREATE)
        }
    }
}