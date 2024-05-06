/**
 * @file: SetAllPointsOnMapActivity.kt
 * @author: Jozef Michal Bukas <xbukas00@stud.fit.vutbr.cz,jozefmbukas@gmail.com>
 * Description: File containing SetAllPointsOnMapActivity class
 */
package com.example.jmb_bms.activities

import android.os.Bundle
import androidx.activity.ComponentActivity
import com.example.jmb_bms.model.database.points.PointDBHelper

/**
 * Activity which is set as callback when users touches any point on map so that they will be refreshed if Locus did
 * not update some point on map. It extends ComponentActivity.
 */
class SetAllPointsOnMapActivity: ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val helper = PointDBHelper(this,null)
        helper.sendAllPointsToLoc(this)
        helper.close()

        finish()
    }
}