package com.example.jmb_bms.activities

import android.os.Bundle
import androidx.activity.ComponentActivity
import com.example.jmb_bms.model.database.points.PointDBHelper

class DummyActivity: ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        PointDBHelper(this,null).sendAllPointsToLoc(this)
        finish()
    }
}