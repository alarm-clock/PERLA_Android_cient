package com.example.jmb_bms

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.jmb_bms.data.TeamSCFrgEnum

class TeamSCConnecting : Fragment(R.layout.team_screen_connecting_wait) {

    private lateinit var activity : TeamScreen
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.team_screen_connecting_wait,container,false)
    }

    override fun onStart() {
        super.onStart()
        activity = requireActivity() as TeamScreen
        val teamSCData = (requireActivity() as TeamScreen).teamSCData

        println("conSC : Waiting in main thread")
        Errors.latch.await()
        println("conSC : Done waiting, websocket -> ${if(Errors.hasErr()) "has encountered error" else "started without problem"} ")
        if( Errors.hasErr())
        {
            println("conSC: in hasErr branch...")
            if(teamSCData.isChecked()) teamSCData.hasUnchecked()
            teamSCData.hasDisconnected()
            if(activity.getPrevFrg() == TeamSCFrgEnum.FRGFILL) activity.changeFragments(this,activity.fragFill)
            else activity.changeFragments(this,activity.fragMain)
            //return
        } else
        {
            println("conSC: in no err branch...")
            teamSCData.hasConnect()
            if(teamSCData.isChecked())activity.startSharingLocationWithServer()
            activity.changeFragments(this,activity.fragMain)
        }
    }
}