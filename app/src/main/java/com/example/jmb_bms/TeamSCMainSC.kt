package com.example.jmb_bms

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Switch
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.jmb_bms.data.TeamSCData

class TeamSCMainSC() : Fragment(R.layout.team_screen) {

    private lateinit var slider: Switch
    private lateinit var hostTxt: TextView
    private lateinit var portTxt: TextView
    private lateinit var editBtn: Button
    private lateinit var connectionBtn: Button
    private lateinit var teamSCData : TeamSCData
    fun changeForFillFrag()
    {
        parentFragmentManager.beginTransaction()
            .remove((requireActivity() as TeamScreen).fragMain)
            .add( R.id.teamSCFrame, (requireActivity() as TeamScreen).fragFill)
            .commit()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        println("In main SC")

    }
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.team_screen,container,false)
    }
    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        teamSCData = (requireActivity() as TeamScreen).teamSCData
        slider = view.findViewById(R.id.teamSCShareLocSwitch)
        hostTxt = view.findViewById(R.id.teamSCHostTxt)
        portTxt = view.findViewById(R.id.teamSCPortTxt)
        editBtn = view.findViewById(R.id.teamSCEditConfBtn)
        connectionBtn = view.findViewById(R.id.teamSCConnectBtn)

        hostTxt.text = teamSCData.ipAddress
        portTxt.text = teamSCData.port.toString()
        slider.text = "Share location"




    }

    override fun onStart() {
        super.onStart()
        hostTxt.text = teamSCData.ipAddress
        portTxt.text = teamSCData.port.toString()
        slider.isChecked = teamSCData.isChecked()
        slider.setOnCheckedChangeListener{_ , isChecked ->

            if(teamSCData.isChecked()) teamSCData.hasUnchecked() else teamSCData.check()
            if(isChecked)
            {
                (requireActivity() as TeamScreen).startService(Intent(activity,PeriodicBackroundPositionUpdater::class.java))

            } else
            {
                (requireActivity() as TeamScreen).stopService(Intent(activity,PeriodicBackroundPositionUpdater::class.java))
            }
        }
        editBtn.setOnClickListener {
            (requireActivity() as TeamScreen).stopService(Intent(activity,PeriodicBackroundPositionUpdater::class.java))
            //routine sending packet to server that it is stopping sharing location
            teamSCData.hasUnchecked()
            changeForFillFrag()
        }
    }

}