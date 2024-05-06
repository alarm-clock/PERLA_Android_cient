/**
 * @file: ConnectionDataAndState.kt
 * @author: Jozef Michal Bukas <xbukas00@stud.fit.vutbr.cz,jozefmbukas@gmail.com>
 * Description: File containing ConnectionDataAndState class
 */
package com.example.jmb_bms.connectionService.models

import android.content.Context
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.util.Log
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.core.content.edit
import com.example.jmb_bms.connectionService.ConnectionService
import com.example.jmb_bms.connectionService.ConnectionState
import com.example.jmb_bms.connectionService.in_app_communication.InnerCommunicationCentral
import com.example.jmb_bms.model.RefreshVals
import com.example.jmb_bms.model.icons.Symbol
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import locus.api.android.ActionDisplayPoints
import locus.api.android.objects.PackPoints
import locus.api.objects.extra.Location
import locus.api.objects.geoData.Point
import locus.api.objects.styles.GeoDataStyle
import java.io.ByteArrayOutputStream
import java.util.Collections
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.CopyOnWriteArraySet

/**
 * Class that serves as main service model and holds connection data and users
 * @param service [ConnectionService]
 * @param comCentral For invoking callbacks
 * @constructor Initializes user information from shared preferences
 */
class ConnectionDataAndState(val service: ConnectionService, val comCentral: InnerCommunicationCentral) {

    private val context = service
    val shpref: SharedPreferences = context.getSharedPreferences("jmb_bms_Server_Info", Context.MODE_PRIVATE)

    lateinit var teamModel: TeamRelatedDataModel

    val profile: UserProfile

    var host: String
    var port: Int

    var serverId: String? = null
        set(value) {
            field = value
            shpref.edit {
                putString("ServerInfo_ServerId",value)
                apply()
            }
        }
    var isConnected: Boolean = false
    var error: Boolean = false
    var errorString: String = ""
        set(value) {
            field = value
            comCentral.sendUpdatedErrString(value)
        }

    var locationShErrorString = ""
    var locationRefreshRate : Long
    var sharingLocation = false
        set(value) {
            field = value
            shpref.edit {
                putBoolean("Server_LocSh",value)
                commit()
            }
            comCentral.updateLocationSharing(value)
        }

    var connectionState = ConnectionState.NOT_CONNECTED
        set(value) {
            field = value
            comCentral.sendUpdatedState(value)
        }

    var listOfUsers = CopyOnWriteArrayList<UserProfile>()

    var period: Long = 5000
    init {
        val userName = shpref.getString("ServerInfo_User", "") ?: ""
        val symbolCode = shpref.getString("ServerInfo_Symbol", "") ?: ""
        val id = shpref.getString("ServerInfo_ServerId","") ?: ""
        host = shpref.getString("ServerInfo_IP","") ?: ""
        val _port = shpref.getString("ServerInfo_Port","0") ?: "0"
        port = if(_port.isEmpty()) 0 else _port.toInt()
        locationRefreshRate = shpref.getLong("ServerInfo_LocRef", 5000)

        profile = UserProfile(userName,id,symbolCode,context,null)

        if(userName == "" || symbolCode == "" || host == "")
        {
            error = true
            errorString = "Can not connect without username and symbol code"
            connectionState = ConnectionState.ERROR
        }
        //val a = Collections.synchronizedList(listOfUsers)
    }

    /**
     * Method that removes all users
     */
    fun clearUsers()
    {
        while (listOfUsers.isNotEmpty())
        {
            val profile = listOfUsers.first()
            listOfUsers.removeAndSend(profile)
        }
    }

    /**
     * Method that sends user point to Locus Map
     * @param userProfile User profile whose location will be shown on map
     * @param ctx CContext for symbol rendering
     */
    private fun sendUserPointToLocus(userProfile: UserProfile, ctx: Context)
    {
        val loc = userProfile.location ?: return
        val point = Point(userProfile.userName,loc)

        val bitmap = Symbol(userProfile.symbolCode,ctx).imageBitmap?.asAndroidBitmap()
        val baos = ByteArrayOutputStream()
        bitmap?.compress(Bitmap.CompressFormat.JPEG,100,baos)

        var packPoints = PackPoints(userProfile.userName)
        packPoints.bitmap = bitmap
        packPoints.addPoint(point)
        ActionDisplayPoints.sendPackSilent(ctx,packPoints,false)
    }

    private fun udpateUser(updatedProfile: UserProfile, storedProfile: UserProfile)
    {
        val oldUsername = storedProfile.userName
        if(updatedProfile.userName != storedProfile.userName) storedProfile.userName = updatedProfile.userName
        if(updatedProfile.symbolCode != storedProfile.symbolCode) storedProfile.symbolCode = updatedProfile.symbolCode
        storedProfile.location = updatedProfile.location
        ActionDisplayPoints.removePackFromLocus(context,oldUsername)
        sendUserPointToLocus(storedProfile,context)
    }

    /**
     * Method that creates user from message
     * @param params Parsed JSON string
     * @param ctx Context for symbol rendering
     */
    fun createUser(params: Map<String, Any?> , ctx: Context)
    {

        val userName = params["userName"] as? String ?: return
        val symbolCode = params["symbolCode"] as? String ?: return
        val serverId = params["_id"] as? String ?: return

        val lat = (params["location"] as? Map<String, Any?>)?.get("lat") as? Double
        val long =  (params["location"] as? Map<String, Any?>)?.get("long") as? Double
        val location: Location?
        location = if(lat == null || long == null) null
        else Location(lat,long)

        val teamEntry = params["teamEntry"] as? List<String> ?: return

        if(userName == this.profile.userName) return
        val existingUser = listOfUsers.find { it.serverId == serverId }

        val newProfile = UserProfile(userName ,serverId, symbolCode, ctx ,location, CopyOnWriteArraySet(teamEntry))

        if(existingUser != null)
        {
            return
        } else
        {
            listOfUsers.addAndSend(newProfile)

            sendUserPointToLocus(newProfile,ctx)
            teamModel.manageTeamEntry(newProfile,true)
        }
    }

    /**
     * Method that removes users point from locus
     * @param userId ID of user whose point will be removed
     * @param ctx Context for Locus method
     */
    private fun removeUsersPointFromLocus(userId: String,ctx: Context)
    {
        val user = listOfUsers.find { it.serverId == userId } ?: return
        ActionDisplayPoints.removePackFromLocus(ctx, user.userName)
        user.location = null
    }

    /**
     * Method that parses location update
     * @param params Parsed JSON string
     * @param ctx Context for symbol rendering and Locus operation
     */
    fun updateUsersLocation(params: Map<String, Any?> , ctx: Context)
    {
        CoroutineScope(Dispatchers.IO).launch {
            Log.d("Service Model","In update users location function")
            val userId = params["_id"] as? String ?: return@launch
            val newlat = params["lat"] as? Double
            val newLong = params["long"] as? Double
            Log.d("Service Model","Extracted userId: $userId -- newLat: $newlat -- newLong: $newLong")

            Log.d("Service Model","Finding user in list...")
            val user = listOfUsers.find { it.serverId == userId } ?: return@launch
            Log.d("Service Model","Found user")

            if(newlat == null || newLong == null)
            {
                Log.d("Service Model","Some parameter was null so removing point from locus")
                removeUsersPointFromLocus(userId, ctx)
                user.location = null

            } else {

                val location = Location(newlat,newLong)
                user.location = location

                Log.d("Service Model","Sending updated location to locus...")
                sendUserPointToLocus(user, ctx)
            }
            comCentral.sendUpdatedUserProfile(user)
        }

    }

    /**
     * Method that deletes user from [listOfUsers] and removes his point from map
     * @param userId ID of user that will be removed
     * @param ctx Context for Locus method
     */
    fun removeUserAndHisLocation(userId: String, ctx: Context)
    {
        Log.d("Service Model","Removing user with id: $userId")
        val user = listOfUsers.find { it.serverId == userId } ?: return
        Log.d("Service Model","Removing his point from Locus")
        removePoint(user,ctx)
        Log.d("Service Model","Removing user from list of users")
        listOfUsers.removeAndSend(user)
        teamModel.manageTeamEntry(user,false)
    }

    /**
     * Method that removes users point from map
     * @param userProfile [UserProfile] of user whose point will be removed
     * @param ctx Context for locus method
     */
    fun removePoint(userProfile: UserProfile, ctx: Context)
    {
        ActionDisplayPoints.removePackFromLocus(ctx,userProfile.userName)
    }

    /**
     * Method that updates user profile from message
     * @param params Parsed JSON string
     * @param ctx Context for Locus method
     */
    fun changeUserProfile(params: Map<String, Any?>, ctx: Context)
    {
        val id = params["_id"] as? String
        val userName = params["userName"] as? String
        val symbolCode = params["symbolCode"] as? String

        val user =  listOfUsers.find { it.serverId == id } ?: return //maybe create him if he does not exist

        ActionDisplayPoints.removePackFromLocus(ctx,user.userName)

        user.userName = userName ?: user.userName
        user.symbolCode = symbolCode ?: user.symbolCode

        sendUserPointToLocus(user,ctx)
    }

    fun manageTeamEntry(params: Map<String, Any?>)
    {
        val id = params["_id"] as? String ?: return
        val profileId = params["profileId"] as? String ?: return
        val adding = params["adding"] as? Boolean ?: return

        val userProfile = if(profileId == profile.serverId) profile else listOfUsers.find { it.serverId == profileId } ?: return

        if(adding) userProfile.teamEntry.add(id)
        else userProfile.teamEntry.remove(id)
        comCentral.sendUpdateTeammateList(id,userProfile,adding)
    }

    /**
     * Method that starts or stops location sharing from message
     * @param params Parsed JSON string
     */
    suspend fun manageLocationShareStateTeamWide(params: Map<String, Any?>)
    {
        val teamId = params["_id"] as? String ?: return
        val on = params["on"] as? Boolean ?: return
        Log.d("ConnectionDataAndState","Setting location share on request of $teamId into state $on")
        manageLocationShareState(on)
    }

    /**
     * Method that toggles location sharing for user
     */
    suspend fun manageIndividualLocationShareChange()
    {
        manageLocationShareState(!sharingLocation)
    }

    /**
     * Method that starts or stops location sharing based on [on] flag
     * @param on Flag indicating if location sharing will be turned on/off
     */
    suspend fun manageLocationShareState(on:Boolean)
    {
        val periodStr = context.getSharedPreferences("jmb_bms_Server_Info", Context.MODE_PRIVATE).getString("Refresh_Val","5s") ?: "5s"
        val period = RefreshVals.entries.toList().find { it.menuString == periodStr }?.delay?.inWholeMilliseconds
        sharingLocation = if(on) {
            service.startSharingLocation(period ?: RefreshVals.S5.delay.inWholeMilliseconds)
            true
        } else {
            service.stopSharingLocation()
            false
        }
    }

    /**
     * Extension method that adds user to list but also invokes callback for observer
     * @param profile Profile that will be added
     */
    private fun MutableList<UserProfile>.addAndSend(profile: UserProfile){
        this.add(profile)
        Log.d("DEBUG1",this.toString())
        comCentral.sendListUpdate(profile,true)
    }

    /**
     * Extension method that removes user from list but also invokes callback for observer
     * @param profile Profile that will be removed
     */
    private fun MutableList<UserProfile>.removeAndSend(profile: UserProfile)
    {
        this.remove(profile)
        comCentral.sendListUpdate(profile,false)
    }
}