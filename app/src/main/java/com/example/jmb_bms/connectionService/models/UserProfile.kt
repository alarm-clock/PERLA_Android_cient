package com.example.jmb_bms.connectionService.models

import android.content.Context
import com.example.jmb_bms.model.icons.Symbol
import locus.api.objects.extra.Location
import java.util.concurrent.CopyOnWriteArraySet


class UserProfile(
    var userName: String,
    var serverId: String,
    symbolString: String,
    val context: Context,
    var location: Location? = null,
    var teamEntry: CopyOnWriteArraySet<String> = CopyOnWriteArraySet()
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

    fun copy() = UserProfile(userName,serverId,symbolCode,context,location, teamEntry)
}