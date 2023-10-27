package com.example.jmb_bms

import android.annotation.SuppressLint
import android.content.Context
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
    private lateinit var errTxt: TextView
    private lateinit var connectionBtn: Button
    private lateinit var teamSCData : TeamSCData
    private var isUserConnected: Boolean = false
    private lateinit var activity : TeamScreen

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
        activity = requireActivity() as TeamScreen
        teamSCData = (requireActivity() as TeamScreen).teamSCData
        slider = view.findViewById(R.id.teamSCShareLocSwitch)
        hostTxt = view.findViewById(R.id.teamSCHostTxt)
        portTxt = view.findViewById(R.id.teamSCPortTxt)
        editBtn = view.findViewById(R.id.teamSCEditConfBtn)
        connectionBtn = view.findViewById(R.id.teamSCConnectBtn)
        errTxt = view.findViewById(R.id.teamSCErrTxt)

        isUserConnected = teamSCData.isConnected()

        hostTxt.text = teamSCData.ipAddress
        portTxt.text = teamSCData.port.toString()
        slider.text = "Share location"

        slider.isActivated = teamSCData.isConnected()

        if( Errors.hasErr())
        {
            errTxt.text = Errors.getLastConErrAndResetErr().toString()
        } else
        {
            errTxt.text = ""
        }


    }

    @SuppressLint("SetTextI18n")
    override fun onStart() {
        super.onStart()

        hostTxt.text = teamSCData.ipAddress
        portTxt.text = teamSCData.port.toString()
        slider.isChecked = teamSCData.isChecked()
        slider.setOnCheckedChangeListener{_ , isChecked ->

            if(teamSCData.isChecked()) teamSCData.hasUnchecked() else teamSCData.check()
            if(isChecked && teamSCData.isConnected())
            {
                activity.startSharingLocationWithServer()

            } else if( !isChecked && teamSCData.isConnected())
            {
                activity.stopSharingLocationWithServer()
                //(requireActivity() as TeamScreen).stopService(Intent(activity,PeriodicBackroundPositionUpdater::class.java))
            }
        }
        editBtn.setOnClickListener {
            activity.stopSharingLocationWithServer()
            if( activity.getBound()) activity.unbindService(activity.getCOnnection())
            activity.setBoundToFalse()
            activity.stopService(Intent(activity,PeriodicBackroundPositionUpdater::class.java))
            //routine sending packet to server that it is stopping sharing location
            teamSCData.hasUnchecked()
            teamSCData.hasDisconnected()
            activity.changeFragments(this,activity.fragFill)
        }
        connectionBtn.text = if( isUserConnected ) "Disconnect" else "Connect"
        connectionBtn.setOnClickListener {
            isUserConnected = !isUserConnected
            if(isUserConnected) teamSCData.hasConnect() else teamSCData.hasDisconnected()

            if( !isUserConnected )
            {
               // activity.stopService(Intent(activity,PeriodicBackroundPositionUpdater::class.java))
                connectionBtn.text = "Connect"
                slider.isChecked = false
                slider.isActivated =false
                println("setOnClickListener: calling stop service...")
                activity.unbindService(activity.getCOnnection())
                activity.setBoundToFalse()
                activity.stopService(Intent(activity,PeriodicBackroundPositionUpdater::class.java))

            } else
            {
                //connectionBtn.text = "Disconnect"
                activity.startServiceAndBind()
                slider.isActivated = true
                activity.changeFragments(this,activity.fragConn)
            }


        }
    }

}