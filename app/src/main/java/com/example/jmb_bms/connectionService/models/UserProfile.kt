/**
 * @file: UserProfile.kt
 * @author: Jozef Michal Bukas <xbukas00@stud.fit.vutbr.cz,jozefmbukas@gmail.com>
 * Description: File containing UserProfile class
 */
package com.example.jmb_bms.connectionService.models

import android.content.Context
import com.example.jmb_bms.model.icons.Symbol
import locus.api.objects.extra.Location
import java.util.concurrent.CopyOnWriteArraySet

/**
 * Class representing user profile
 * @param userName Username
 * @param serverId ID
 * @param symbolString symbol code
 * @param context Context for rendering
 * @param location Users location or null if he isn't sharing
 * @param teamEntry Team ids of all teams in which user is
 */
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

    fun copy() = UserProfile(userName,serverId,symbolCode,context,location, teamEntry)
}