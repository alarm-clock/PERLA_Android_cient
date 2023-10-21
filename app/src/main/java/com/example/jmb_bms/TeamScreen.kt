package com.example.jmb_bms

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
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
import java.util.*
import java.util.concurrent.TimeUnit

class TeamScreen : AppCompatActivity() {

    lateinit var teamSCData: TeamSCData
    lateinit var fragFill: TeamSCHostAndPortFill
    lateinit var fragMain: TeamSCMainSC

    fun showFillFrag(fragFill: TeamSCHostAndPortFill){
        supportFragmentManager.beginTransaction()
            .show(fragFill)
            .commit()
    }
    fun hideFillFrag(fragFill: TeamSCHostAndPortFill)
    {
        supportFragmentManager.beginTransaction()
            .hide(fragFill)
            .commit()
    }
    fun showMainFrag(fragTeamSCMainSC: TeamSCMainSC)
    {
        supportFragmentManager.beginTransaction()
            .show(fragTeamSCMainSC)
            .commit()
    }
    fun hideMainFrag(fragTeamSCMainSC: TeamSCMainSC)
    {
        supportFragmentManager.beginTransaction()
            .hide(fragTeamSCMainSC)
            .commit()
    }

    fun createFragment(frag: Fragment , viewId: Int )
    {
        supportFragmentManager.beginTransaction()
            .add( viewId,frag)
            .commit()
        /*supportFragmentManager.beginTransaction()
            .add(R.id.teamSCFrame2,TeamSCMainSC())
            .hide(TeamSCMainSC())
            .commit()*/
    }
    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val sharedPref = getSharedPreferences("TeamSCSharedPref", MODE_PRIVATE)
        teamSCData = TeamSCData(sharedPref)

        setContentView(R.layout.team_screen_frame)

        fragFill = TeamSCHostAndPortFill()
        fragMain = TeamSCMainSC()

        /*if(teamSCData.hasIPAndPort()) showMainFrag(fragMain)
        else
        {
            println("here1")
            showFillFrag(fragFill)
            println("here2")
        }*/
        if( teamSCData.hasIPAndPort()) createFragment(fragMain, R.id.teamSCFrame2)
        else createFragment(fragFill,R.id.teamSCFrame)

    }
}