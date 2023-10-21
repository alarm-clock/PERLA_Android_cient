package com.example.jmb_bms

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.LayoutManager
import com.example.jmb_bms.data.ListData
import locus.api.android.objects.LocusVersion
import locus.api.android.utils.IntentHelper
import locus.api.android.utils.LocusUtils
import locus.api.objects.extra.Location

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val version = LocusUtils.getAvailableVersions(this)
        if( version.isEmpty() ) return

        if(IntentHelper.isIntentMainFunction(intent))
        {
            IntentHelper.handleIntentMainFunction(this,intent, object : IntentHelper.OnIntentReceived{

                override fun onReceived(lv: LocusVersion, locGps: Location?, locMapCenter: Location?) {
                    setContentView(R.layout.main_menu)

                    //val dataset = arrayOf("Chat" , "Orders" , "Team" , "Settings" , "Points Management")
                    val customAdapter = MainMenuCustomAdapter(ListData().loadAffirmaions())
                    customAdapter.onItemClick = {
                        println(it.id)
                    }

                    val recyclerView: RecyclerView = findViewById(R.id.recView)
                    recyclerView.layoutManager = LinearLayoutManager(this@MainActivity)
                    recyclerView.adapter = customAdapter
                }

                override fun onFailed() {

                }
            })

        }
        else if( IntentHelper.isIntentPointTools(intent))
        {
            setContentView(R.layout.main_menu)

            //val dataset = arrayOf("Chat" , "Orders" , "Team" , "Settings" , "Points Management")
            val customAdapter = MainMenuCustomAdapter(ListData().loadAffirmaions())
            customAdapter.onItemClick = {
                println(it.id)
                if(it.id == 5){
                    val intentTeamScreen = Intent(this , TeamScreen::class.java )
                    startActivity(intentTeamScreen)
                }
            }

            val recyclerView: RecyclerView = findViewById(R.id.recView)
            recyclerView.layoutManager = LinearLayoutManager(this@MainActivity)
            recyclerView.adapter = customAdapter
        }
        else
        {
            LocusUtils.callStartLocusMap(this)
        }

    }
}

