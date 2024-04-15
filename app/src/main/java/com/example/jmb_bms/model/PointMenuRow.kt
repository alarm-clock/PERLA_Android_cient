package com.example.jmb_bms.model

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.jmb_bms.model.icons.Symbol

class PointMenuRow(val id: Long, name: String, symbol: Symbol, visible: Boolean, var ownedByClient: Boolean) {

    val liveName = MutableLiveData(name)
    val liveSymbol = MutableLiveData(symbol)
    val liveVisible = MutableLiveData(visible)

}