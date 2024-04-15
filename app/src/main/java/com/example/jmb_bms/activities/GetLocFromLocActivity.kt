package com.example.jmb_bms.activities

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import locus.api.android.ActionBasics
import locus.api.android.utils.IntentHelper

class GetLocFromLocActivity: ComponentActivity() {

    private var caller: Int? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if(IntentHelper.isIntentReceiveLocation(intent))
        {
            val pt = IntentHelper.getPointFromIntent(this,intent)

            val intent = Intent(this, PointActivity::class.java).apply {
                putExtra("caller","GetLoc")
            }

            pt?.let {
                val bundle = Bundle()
                bundle.putDouble("lat",it.location.latitude)
                bundle.putDouble("long",it.location.longitude)

                intent.putExtra("location",bundle)
            }
            startActivity(intent)
            finish()

        } else if( intent.getStringExtra("caller") != null)
        {
            val callerString = intent.getStringExtra("caller")
            when(callerString)
            {
                "point" -> caller = 1
                else -> caller = 0
            }
            ActionBasics.actionPickLocation(this)
            finish()
        } else
        {
            finish()
        }

    }

    //TODO store caller in shpref or file
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        caller?.let {
            outState.putInt("caller",it)
        }

    }
}