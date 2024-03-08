package com.example.jmb_bms.connectionService

import android.content.Context
import com.example.jmb_bms.model.Symbol
import locus.api.objects.extra.Location


class UserProfile(
    var userName: String,
    var serverId: String,
    symbolString: String,
    val context: Context,
    var location: Location? = null
) {
    var symbolCode: String = symbolString
        set(value) {
            field = value
            symbol = Symbol(value,context)
        }
    var symbol: Symbol? = null

    init {
        symbol = Symbol(symbolCode,context)
    }
}