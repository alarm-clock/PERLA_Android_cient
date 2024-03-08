package com.example.jmb_bms.connectionService

import android.content.Context
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.util.Log
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.core.content.edit
import locus.api.android.ActionDisplayPoints
import locus.api.android.ActionMapTools
import locus.api.android.objects.PackPoints
import locus.api.objects.extra.Location
import locus.api.objects.geoData.Point
import locus.api.objects.styles.GeoDataStyle
import java.io.ByteArrayOutputStream
import java.util.Collections

class connectionDataAndState(val context: Context, val comCentral: InnerCommunicationCentral) {

    val shpref: SharedPreferences = context.getSharedPreferences("jmb_bms_Server_Info", Context.MODE_PRIVATE)

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
            comCentral.updateLocationSharing(value)
        }

    var connectionState = ConnectionState.NOT_CONNECTED
        set(value) {
            field = value
            comCentral.sendUpdatedState(value)
        }

    var listOfUsers: MutableList<UserProfile> = Collections.synchronizedList(mutableListOf<UserProfile>())

    var userIsTeamLeader = false

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
        val a = Collections.synchronizedList(listOfUsers)
    }

    private fun sendUserPointToLocus(userProfile: UserProfile,ctx: Context)
    {
        val loc = userProfile.location ?: return
        val point = Point(userProfile.userName,loc)
        val style = GeoDataStyle()
        val bitmap = userProfile.symbol?.imageBitmap?.asAndroidBitmap()
        val baos = ByteArrayOutputStream()
        bitmap?.compress(Bitmap.CompressFormat.JPEG,100,baos)
        val biteArr = baos.toByteArray()
        val base64 = android.util.Base64.encodeToString(biteArr,android.util.Base64.DEFAULT)
        var packPoints = PackPoints(userProfile.userName)
        packPoints.bitmap = bitmap
        packPoints.addPoint(point)
        ActionDisplayPoints.sendPackSilent(ctx,packPoints,false)
    }

    private fun udpateUser(updatedProfile: UserProfile,storedProfile: UserProfile)
    {
        val oldUsername = storedProfile.userName
        if(updatedProfile.userName != storedProfile.userName) storedProfile.userName = updatedProfile.userName
        if(updatedProfile.symbolCode != storedProfile.symbolCode) storedProfile.symbolCode = updatedProfile.symbolCode
        storedProfile.location = updatedProfile.location
        ActionDisplayPoints.removePackFromLocus(context,oldUsername)
        sendUserPointToLocus(storedProfile,context)
    }
    fun createUser(params: Map<String, Any?> , ctx: Context)
    {
        Log.d("Service Model","Creating user")
        val userName = params["userName"] as? String ?: return
        Log.d("Service Model","Extracted username: $userName")

        val symbolCode = params["symbolCode"] as? String ?: return
        Log.d("Service Model","Extracted symbolCode: $symbolCode")

        val serverId = params["_id"] as? String ?: return
        Log.d("Service Model","Extracted id: $serverId")

        val lat = (params["location"] as? Map<String, Any?>)?.get("lat") as? Double
        val long =  (params["location"] as? Map<String, Any?>)?.get("long") as? Double
        val location: Location?
        location = if(lat == null || long == null) null
        else Location(lat,long)

        if(userName == this.profile.userName) return
        val existingUser = listOfUsers.find { it.serverId == serverId }

        val newProfile = UserProfile(userName ,serverId, symbolCode, ctx ,location)

        if(existingUser != null)
        {

        } else
        {
            Log.d("Service Model","Adding new user to listOfUsers and sending point to locus... Location is: $location")
            listOfUsers.addAndSend(newProfile)
            sendUserPointToLocus(newProfile,ctx)
        }
    }

    private fun removeUsersPointFromLocus(userId: String,ctx: Context)
    {
        val user = listOfUsers.find { it.serverId == userId } ?: return
        ActionDisplayPoints.removePackFromLocus(ctx, user.userName)
        user.location = null
    }
    fun updateUsersLocation(params: Map<String, Any?> , ctx: Context)
    {
        Log.d("Service Model","In update users location function")
        val userId = params["_id"] as? String ?: return
        val newlat = params["lat"] as? Double
        val newLong = params["long"] as? Double
        Log.d("Service Model","Extracted userId: $userId -- newLat: $newlat -- newLong: $newLong")


        if(newlat == null || newLong == null)
        {
            Log.d("Service Model","Some parameter was null so removing point from locus")
            removeUsersPointFromLocus(userId, ctx)
        } else {

            val location = Location(newlat,newLong)
            Log.d("Service Model","Finding user in list...")
            val user = listOfUsers.find { it.serverId == userId } ?: return
            Log.d("Service Model","Found user")

            user.location = location

            Log.d("Service Model","Sending updated location to locus...")
            sendUserPointToLocus(user, ctx)
        }
    }
    fun removeUserAndHisLocation(userId: String, ctx: Context)
    {
        Log.d("Service Model","Removing user with id: $userId")
        val user = listOfUsers.find { it.serverId == userId } ?: return
        Log.d("Service Model","Removing his point from Locus")
        ActionDisplayPoints.removePackFromLocus(ctx,user.userName)
        Log.d("Service Model","Removing user from list of users")
        listOfUsers.removeAndSend(user)
    }

    //TODO profile must also be live data so and should be updated as list itself
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

    private fun MutableList<UserProfile>.addAndSend(profile: UserProfile){
        this.add(profile)
        comCentral.sendListUpdate(profile,true)
    }

    private fun MutableList<UserProfile>.removeAndSend(profile: UserProfile)
    {
        this.remove(profile)
        comCentral.sendListUpdate(profile,false)
    }
}