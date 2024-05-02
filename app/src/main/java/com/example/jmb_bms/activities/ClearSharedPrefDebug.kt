package com.example.jmb_bms.activities

import android.content.SharedPreferences
import android.os.Bundle
import androidx.activity.ComponentActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ClearSharedPrefDebug : ComponentActivity() {

    private fun SharedPreferences.editPreferences(operation: (SharedPreferences.Editor) -> Unit)
    {
        val scope = CoroutineScope(Dispatchers.IO)

        scope.launch {
            withContext(Dispatchers.IO)
            {
                val ed = edit()
                operation(ed)
                ed.apply()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val shPref = getSharedPreferences("jmb_bms_Server_Info", MODE_PRIVATE)

        shPref.editPreferences {
            it.putString("ServerInfo_IP", "")
            it.putString("ServerInfo_Port", "")
            it.putString("ServerInfo_User", "")
            it.putString("ServerInfo_IconMenuLists","")
            it.putString("ServerInfo_Symbol", "")

            it.putString("OrderSH","1-2-3")

            it.putBoolean("ServerInfo_IPent", false)
            it.putBoolean("ServerInfo_PortEnt", false)
            it.putBoolean("ServerInfo_UserEnt", false)
            it.putBoolean("Service_Running",false)
            it.putBoolean("Normal_Screen",false)
        }

        finish()
    }
}