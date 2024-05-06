/**
 * @file: GetLocFromActivity.kt
 * @author: Jozef Michal Bukas <xbukas00@stud.fit.vutbr.cz,jozefmbukas@gmail.com>
 * Description: File containing GetLocFromActivity class
 */
package com.example.jmb_bms.activities

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import locus.api.android.ActionBasics
import locus.api.android.utils.IntentHelper

/**
 * Activity which is called when some method or activity needs to obtain location from map in Locus Map. Right now only
 * one method uses this feature but in the future more methods/activities might use this feature. Then just remove hardcoded
 * part store caller in shared preferences. This activity exists mainly because PickLocation activity from Locus does not
 * return any value after it ends but instead calls activity with given intent filter anf that can be only one activity.
 */
class GetLocFromLocActivity: ComponentActivity() {

    private var caller: Int? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //called from locus
        if(IntentHelper.isIntentReceiveLocation(intent))
        {
            val pt = IntentHelper.getPointFromIntent(this,intent)

            val intent = Intent(this, PointActivity::class.java).apply {
                putExtra("caller","GetLoc")
            }

            //locus sends point that can be null so check it and store lat/long in bundle that will be added to intent as an extra
            pt?.let {
                val bundle = Bundle()
                bundle.putDouble("lat",it.location.latitude)
                bundle.putDouble("long",it.location.longitude)

                intent.putExtra("location",bundle)
            }
            startActivity(intent)
            finish()


        //called by some activity from within
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
            //unknown caller
            finish()
        }

    }
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        caller?.let {
            outState.putInt("caller",it)
        }

    }
}