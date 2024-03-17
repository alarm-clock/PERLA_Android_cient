package com.example.jmb_bms.connectionService.models

import android.content.Context
import com.example.jmb_bms.model.icons.Symbol
import locus.api.objects.extra.Location
import java.util.*
import kotlin.collections.HashSet


class TeamProfile(
    val _id: String,
    teamIcon: String,
    var teamName: String,
    var teamLead: String,
    private val appCtx: Context,
    var teamEntry: MutableSet<String> = Collections.synchronizedSet(HashSet<String>())
)
{
    var teamIcon = teamIcon
        set(value) {
            field = value
            teamSymbol = Symbol(value,appCtx)
        }

    var teamSymbol = Symbol(teamIcon,appCtx)

    fun copy() = TeamProfile(_id, teamIcon, teamName, teamLead, appCtx, teamEntry)

    companion object{

        fun createTeamProfileFromParams(params: Map<String, Any?>, appCtx: Context): TeamProfile?
        {
            val _id = params["_id"] as? String ?: return null
            val teamName = params["teamName"] as? String ?: return null
            val teamIcon = params["teamIcon"] as? String ?: return null
            val teamLead = params["teamLead"] as? String ?: return null
            //val teamLocationMap = params["teamLocation"] as? Map<String, Any?> ?: return null
           // val lat = teamLocationMap["lat"] as? Double ?: return null
           // val long = teamLocationMap["long"] as? Double ?: return null
            //prepared for future
           // val teamLocation = Location(lat,long)
            val teamEntry = params["teamEntry"] as? List<String> ?: return null

            return TeamProfile(
                _id,teamIcon,teamName,teamLead,appCtx,teamEntry.toHashSet()
            )
        }
    }
}
