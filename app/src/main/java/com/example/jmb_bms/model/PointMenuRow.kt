/**
 * @file: PointMenuRow.kt
 * @author: Jozef Michal Bukas <xbukas00@stud.fit.vutbr.cz,jozefmbukas@gmail.com>
 * Description: File containing PointMenuRow class
 */
package com.example.jmb_bms.model

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.jmb_bms.model.icons.Symbol

/**
 * Class that holds point data that are shown and used in view
 * @param id Points ID
 * @param name Points name
 * @param symbol Points symbol that will be shown
 * @param visible Flag indicating if point is visible
 * @param ownedByClient Flog indicating if user can update given point
 */
class PointMenuRow(val id: Long, name: String, symbol: Symbol, visible: Boolean, var ownedByClient: Boolean) {

    val liveName = MutableLiveData(name)
    val liveSymbol = MutableLiveData(symbol)
    val liveVisible = MutableLiveData(visible)

}