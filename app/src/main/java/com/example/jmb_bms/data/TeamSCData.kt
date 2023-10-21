package com.example.jmb_bms.data

import android.content.SharedPreferences
import androidx.core.content.edit
import androidx.core.view.MenuHost
import java.util.UUID

class TeamSCData( shPref: SharedPreferences) {

    var ipAddress : String?
    var port: Int
    private var isChecked: Boolean
    private var hasIpAndPort : Boolean
    private var isConnected : Boolean
    private val shPref = shPref

    init {
        ipAddress = shPref.getString("IPAddr" , null)
        port = shPref.getInt("Port" , 0)
        isChecked = shPref.getBoolean("Checked",false)
        hasIpAndPort = shPref.getBoolean("hasIpAndPort", false)
        isConnected = shPref.getBoolean("isConnected" , false)
    }


    fun isChecked() = isChecked
    fun isConnected() = isConnected
    fun hasIPAndPort() = hasIpAndPort

    fun editBoolInPref(name: String , value: Boolean)
    {
        shPref.edit {
            putBoolean(name , value)
            apply()
        }
    }
    fun editStringInPref(name: String,value: String)
    {
        shPref.edit {
            putString(name , value)
            apply()
        }
    }
    fun editIntInPref(name: String , value: Int)
    {
        shPref.edit {
            putInt(name , value)
            apply()
        }
    }

    fun check(){
        isChecked = true
        editBoolInPref("Checked" , isChecked)
    }
    fun putedIPAndPort(){
        hasIpAndPort = true
        editBoolInPref("hasIpAndPort" , hasIpAndPort)
    }
    fun hasConnect(){
        isConnected = true
        editBoolInPref("isConnected" , isConnected)
    }
    fun hasUnchecked()
    {
        isChecked = false
        editBoolInPref("Checked" , isChecked)
    }
    fun hasDeletedIPAndPort()
    {
        hasIpAndPort = false
        editBoolInPref("hasIpAndPort" , hasIpAndPort)
    }
    fun hasDisconnected()
    {
        isConnected = false
        editBoolInPref("isConnected" ,isConnected)
    }
    fun editHost( host: String) {
        ipAddress = host
        editStringInPref("IPAddr", host)
    }
    fun editPort( port: Int)
    {
        this.port = port
        editIntInPref("Port",port)
    }

}