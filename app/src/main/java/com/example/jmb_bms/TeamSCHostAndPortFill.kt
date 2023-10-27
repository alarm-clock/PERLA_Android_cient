package com.example.jmb_bms

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.lifecycle.findViewTreeViewModelStoreOwner
import androidx.lifecycle.viewmodel.CreationExtras
import com.example.jmb_bms.data.TeamSCData
import io.ktor.client.*
import io.ktor.client.plugins.websocket.*
import io.ktor.http.*
import io.ktor.websocket.*
import kotlinx.coroutines.runBlocking
import java.lang.NumberFormatException
import kotlin.concurrent.thread

class TeamSCHostAndPortFill() : Fragment(R.layout.team_screen_filling_ip_and_port) {

    private lateinit var editHostTxt : EditText
    private lateinit var editPortTxt : EditText
    private lateinit var conAndShBtn : Button
    private lateinit var conBtn : Button
    private lateinit var errTxt : TextView
    private lateinit var teamSCData : TeamSCData
    private var hasIp = false
    private var hasPort = false
    private lateinit var activity : TeamScreen

    private fun isEditEmpty( editText: EditText) : Boolean
    {
        return editText.text.toString().trim().isEmpty()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        println("In on create function")

    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        println("In on Created view function")
        return inflater.inflate(R.layout.team_screen_filling_ip_and_port,container,false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        this.activity = requireActivity() as TeamScreen
        super.onViewCreated(view, savedInstanceState)
        println("In on View Created function")
        teamSCData = (requireActivity() as TeamScreen).teamSCData
        println("In on view function after requireActivity function")
        editHostTxt = view.findViewById(R.id.teamSCHostEdit)
        editPortTxt = view.findViewById(R.id.teamSCPortEdit)
        conAndShBtn = view.findViewById(R.id.teamSCFillConShBtn)
        conBtn = view.findViewById(R.id.teamSCFillConBtn)
        errTxt = view.findViewById(R.id.teamSCFillErr)

        if( teamSCData.hasIPAndPort() )
        {
            editHostTxt.setText(teamSCData.ipAddress)
            editPortTxt.setText(teamSCData.port.toString())
            hasPort = true
            hasIp = true
        } else {
            editHostTxt.hint = "Host address"
            editPortTxt.hint = "Host port"
            conAndShBtn.isEnabled = false
            conBtn.isEnabled = false
        }
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
        println("In on start function")
        editPortTxt.addTextChangedListener {

            hasPort = it.toString().trim().isNotEmpty()

            if( hasPort && hasIp)
            {
                teamSCData.putedIPAndPort()
                conBtn.isEnabled = true
                conAndShBtn.isEnabled = true
            } else
            {
                teamSCData.hasDeletedIPAndPort()
                conAndShBtn.isEnabled = false
                conBtn.isEnabled =false
            }
        }

        editHostTxt.addTextChangedListener {
            hasIp = it.toString().trim().isNotEmpty()
            if( hasPort && hasIp)
            {
                teamSCData.putedIPAndPort()
                conBtn.isEnabled = true
                conAndShBtn.isEnabled = true
            } else
            {
                teamSCData.hasDeletedIPAndPort()
                conBtn.isEnabled = false
                conBtn.isEnabled = false
            }
        }
        conBtn.setOnClickListener {

            teamSCData.editHost(editHostTxt.text.toString())

            try {
                teamSCData.editPort(editPortTxt.text.toString().toInt())
            } catch ( nfe: NumberFormatException)
            {
                errTxt.text = "Port must be number"
                return@setOnClickListener
            }

            (requireActivity() as TeamScreen).startForegroundService(
                Intent(
                    activity,
                    PeriodicBackroundPositionUpdater::class.java
                )
            )
            activity.startServiceAndBind()

            if( Errors.hasErr() )
            {
                errTxt.text = Errors.getLastConErrAndResetErr().toString()
                return@setOnClickListener
            }
            else errTxt.text = ""
            activity.changeFragments(this,activity.fragConn)

        //connection routine
        }
        conAndShBtn.setOnClickListener {
            teamSCData.check()
            teamSCData.editHost(editHostTxt.text.toString())

            try {
                teamSCData.editPort(editPortTxt.text.toString().toInt())
            } catch ( nfe: NumberFormatException)
            {
                errTxt.text = "Port must be number"
                return@setOnClickListener
            }
            errTxt.text = ""

            activity.startServiceAndBind()

            if( Errors.hasErr() )
            {
                errTxt.text = Errors.getLastConErrAndResetErr().toString()
                return@setOnClickListener
            } else errTxt.text = ""



            activity.changeFragments(this,activity.fragConn)

            //connection and sharing routine
        }

    }
}