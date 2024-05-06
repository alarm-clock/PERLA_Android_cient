/**
 * @file: TeamRelatedDataModel.kt
 * @author: Jozef Michal Bukas <xbukas00@stud.fit.vutbr.cz,jozefmbukas@gmail.com>
 * Description: File containing TeamRelatedDataModel class
 */
package com.example.jmb_bms.connectionService.models

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import androidx.compose.ui.graphics.asAndroidBitmap
import com.example.jmb_bms.connectionService.ClientMessage
import com.example.jmb_bms.connectionService.ConnectionService
import com.example.jmb_bms.connectionService.PeriodicPositionUpdater
import com.example.jmb_bms.connectionService.in_app_communication.InnerCommunicationCentral
import com.example.jmb_bms.model.RefreshVals
import io.ktor.websocket.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import locus.api.android.ActionDisplayPoints
import locus.api.android.objects.PackPoints
import locus.api.objects.extra.Location
import locus.api.objects.geoData.Point
import java.io.ByteArrayOutputStream
import java.util.*
import java.util.concurrent.CopyOnWriteArraySet

/**
 * Class that implements all team related methods for message parsing
 * @param comCentral Used to invoke callbacks
 * @param mainModel Main server model
 * @param service Used to get session
 */
class TeamRelatedDataModel(private val comCentral: InnerCommunicationCentral, private val mainModel: ConnectionDataAndState, val service: ConnectionService) {


    private val serviceContext = service
    //val teams = Collections.synchronizedSet(hashSetOf<Pair<TeamProfile,HashSet<UserProfile>>>())
    val teamLocationUpdateHandlers = Collections.synchronizedSet(hashSetOf<PeriodicPositionUpdater>())

    val teams = CopyOnWriteArraySet<Pair<TeamProfile,CopyOnWriteArraySet<UserProfile>>>()

    /**
     * Method that finds all team members for given team
     * @param teamId ID of team whose members are being found
     * @return Set of member user profiles
     */
    fun findUsersInSameTeam(teamId: String): CopyOnWriteArraySet<UserProfile>
    {
        val set = CopyOnWriteArraySet<UserProfile>()
        mainModel.listOfUsers.forEach { profile ->
           // logInfo("Client id is ${mainModel.profile.serverId} - this id is ")
            val team = profile.teamEntry.find { it == teamId }

            if(team != null){
               // logInfo("In findUsersInSameTeam added user $profile to team")
                set.add(profile)
            }
        }

        val team = mainModel.profile.teamEntry.find { it == teamId }
        if(team != null)
        {
            logInfo("In findUsersInSameTeam added user ${mainModel.profile} to team")
            set.add(mainModel.profile)
        }
        return set
    }

    fun checkIfTeamExists(teamId: String): Boolean = teams.find { it.first._id == teamId } != null

    /**
     * Method that creates team from message
     * @param params Parsed JSON string
     */
    fun createTeam(params: Map<String, Any?>)
    {

        val teamProfile = TeamProfile.createTeamProfileFromParams(params,serviceContext)
        if(teamProfile == null)
        {
            logInfo("Team profile is null!")
            return
        }
        logInfo("Created team with: id ${teamProfile._id} - name: ${teamProfile.teamName} \n" +
                "   symbol: ${teamProfile.teamIcon} - teamLead: ${teamProfile.teamLead}\n" +
                "   teamEntry: ${teamProfile.teamEntry}")

        if(teamProfile.teamLocation != null) sendTeamPointToLocus(teamProfile,serviceContext)
        val pair = Pair(teamProfile, findUsersInSameTeam(teamProfile._id))

        teams.add(pair)
        comCentral.sendTeamsPairUpdate(pair,true)
        logInfo("Added new team")
    }

    /**
     * Method that deletes team
     * @param params Parsed JSON string
     */
    fun deleteTeam(params: Map<String, Any?>)
    {
        logInfo("In deleteTeam function going to delete some team...")
        val id = params["_id"] as? String
        val teamPair = teams.find { it.first._id == id } ?: return
        logInfo("In delete function going to delete team with id $id")

        teams.remove(teamPair)
        comCentral.sendTeamsPairUpdate(teamPair,false)

        stopSharingLocationAsTeam(id!!)
        if(teamPair.first.teamLocation != null) removeTeamsPointFromLocus(id,serviceContext)

        logInfo("Removed team in internal structure and sent update to viewModel")

        mainModel.listOfUsers.forEach {
            it.teamEntry.remove(id)
            comCentral.sendUpdatedUserProfile(it)
        }
        logInfo("Removed all teamEntries with deleted team in user profiles")
    }

    /**
     * Method that changes team leader
     * @param params Parsed JSON message
     */
    fun changeTeamLeader(params: Map<String, Any?>)
    {
        CoroutineScope(Dispatchers.IO).launch {
            val id = params["_id"] as? String ?: return@launch
            val newLeader = params["userId"] as? String ?: return@launch

            val newLeaderProfile = if(mainModel.profile.serverId == newLeader) mainModel.profile else mainModel.listOfUsers.find { it.serverId == newLeader }

            if( newLeaderProfile == null ){
                logInfo("NO user with user Id $newLeader is stored in model")
                return@launch
            }

            val teamProfile = teams.find { it.first._id == id }?.first

            if(teamProfile == null)
            {
                logInfo("No team with Id $id exists in model")
                return@launch
            }
            if(teamProfile.teamLead == mainModel.profile.serverId) stopSharingLocationAsTeam(teamProfile._id)

            teamProfile.teamLead = newLeader
            comCentral.sendUpdatedTeamsProfile(teamProfile)

            if( newLeaderProfile.teamEntry.add(id) )
            {
                comCentral.sendUpdatedUserProfile(newLeaderProfile)
                comCentral.sendUpdateTeammateList(id,newLeaderProfile,true)
            }
        }

    }

    /**
     * Method that updates team data
     * @param params Parsed JSON message
     */
    fun updateTeam(params: Map<String, Any?>)
    {
        CoroutineScope(Dispatchers.IO).launch {
            val id = params["_id"] as? String ?: return@launch
            val newName = params["teamName"] as? String ?: return@launch
            val newIcon = params["teamIcon"] as? String ?: return@launch

            val profile = teams.find{ it.first._id == id} ?: return@launch

            profile.first.teamName = newName
            profile.first.teamIcon = newIcon

            comCentral.sendUpdatedTeamsProfile(profile.first)
        }

    }

    /**
     * Method that adds or removes team from user team entries
     * @param id Team ID
     * @param userProfile [UserProfile] in which team will be added or removed
     * @param adding Flag that indicates if team will be added or removed
     */
    private fun updateTeamEntry(id: String, userProfile: UserProfile,adding: Boolean)
    {
        if(adding)
        {
            userProfile.teamEntry.add(id)
            teams.find { it.first._id == id }?.second?.add(userProfile)
        }
        else
        {
            userProfile.teamEntry.remove(id)
            teams.find { it.first._id == id }?.second?.remove(userProfile)
            logInfo(teams.find { it.first._id == id }?.first.toString())
        }
        comCentral.sendUpdateTeammateList(id,userProfile,adding)
    }

    /**
     * Method that updates team entries for user profile
     * @param newUserProfile User profile that will be updated
     * @param add Flag that indicates if team will be added or removed
     */
    fun manageTeamEntry(newUserProfile: UserProfile, add: Boolean)
    {
        newUserProfile.teamEntry.forEach {
            updateTeamEntry(it,newUserProfile,add)
        }
    }

    /**
     * Method that manages team entries from message
     * @param params Parsed JSON string
     */
    fun manageTeamEntry(params: Map<String, Any?>)
    {
        val id = params["_id"] as? String ?: return
        val profileId = params["profileId"] as? String ?: return
        val adding = params["adding"] as? Boolean ?: return

        val userProfile = if(profileId == mainModel.profile.serverId) mainModel.profile else mainModel.listOfUsers.find { it.serverId == profileId } ?: return

        updateTeamEntry(id,userProfile,adding)
    }

    /**
     * Method that updates teams location
     * @param params Parsed JSON string
     */
    fun parseTeamLocUpdate(params: Map<String, Any?>)
    {
        CoroutineScope(Dispatchers.IO).launch {
            val id = params["_id"] as? String ?: return@launch
            val profile = teams.find { it.first._id == id }?.first ?: return@launch

            if(profile.teamLead == mainModel.profile.serverId) return@launch //this is my location


            val lat = params["lat"] as? Double
            val long = params["long"] as? Double

            if(lat == null || long == null)
            {
                profile.teamLocation = null
                removeTeamsPointFromLocus(id,serviceContext)

            } else
            {
                if(profile.teamLocation == null) profile.teamLocation = Location(lat,long)
                else {
                    profile.teamLocation?.latitude = lat
                    profile.teamLocation?.longitude = long
                }
                sendTeamPointToLocus(profile,serviceContext)
            }
            comCentral.sendUpdatedTeamsProfile(profile)
        }

    }

    /**
     * Method that sends team symbol on map
     * @param teamProfile Profile of team that will be added
     * @param ctx Context for symbol rendering
     */
    private fun sendTeamPointToLocus(teamProfile: TeamProfile, ctx: Context)
    {
        val loc = teamProfile.teamLocation ?: return
        val point = Point(teamProfile.teamName,loc)

        val bitmap = teamProfile.teamSymbol.imageBitmap?.asAndroidBitmap()
        val baos = ByteArrayOutputStream()
        bitmap?.compress(Bitmap.CompressFormat.JPEG,100,baos)

        var packPoints = PackPoints(teamProfile.teamName)
        packPoints.bitmap = bitmap
        packPoints.addPoint(point)
        ActionDisplayPoints.sendPackSilent(ctx,packPoints,false)
    }

    /**
     * Method for removing team point from map
     * @param teamId ID of team that will be removed
     * @param ctx Used in Locus operation
     */
    private fun removeTeamsPointFromLocus(teamId: String,ctx: Context)
    {
        val team = teams.find { it.first._id  ==  teamId }?.first ?: return
        ActionDisplayPoints.removePackFromLocus(ctx, team.teamName)
        team.teamLocation = null
    }

    /**
     * Method that starts location sharing job for team
     * @param teamId ID of team on which behalf location will be sent
     * @param delay Location refresh period
     */
    fun startSharingLocationAsTeam(teamId: String, delay: RefreshVals)
    {
        val profile = teams.find { it.first._id == teamId }?.first ?: return
        val positionUpdater = PeriodicPositionUpdater(delay.delay.inWholeMilliseconds,serviceContext,service.session,true,teamId,false)
        teamLocationUpdateHandlers.add(positionUpdater)
        profile.thisClientSharingLoc = true
        profile.sharingLocDelay = delay

        comCentral.sendUpdatedTeamsProfile(profile)
    }

    /**
     * Method that stops sharing location on behalf of team
     * @param teamId ID of team whose location sharing will be stopped
     */
    fun stopSharingLocationAsTeam(teamId: String)
    {
        val profile = teams.find { it.first._id == teamId }?.first ?: return

        val updater = teamLocationUpdateHandlers.find { it.teamId == teamId } ?: return
        updater.stopSharingLoc()
        teamLocationUpdateHandlers.remove(updater)
        service.sendMessage(ClientMessage.teamLocationUpdatingStop(teamId))

        profile.thisClientSharingLoc = false
        comCentral.sendUpdatedTeamsProfile(profile)
    }

    /**
     * Method that changes location sharing period for team
     * @param teamId ID of team
     * @param delay new value
     */
    fun changeLocShDelay(teamId: String, delay: RefreshVals)
    {
        val profile = teams.find { it.first._id == teamId }?.first ?: return
        val updater = teamLocationUpdateHandlers.find { it.teamId == teamId } ?: return
        updater.changeDelay(delay.delay.inWholeMilliseconds)
        profile.sharingLocDelay = delay

        comCentral.sendUpdatedTeamsProfile(profile)
    }

    /**
     * Method that resets instance
     */
    fun clearTeams()
    {
        while ( teams.isNotEmpty())
        {
            val team = teams.first()
            removeTeamsPointFromLocus(team.first._id,serviceContext)
            teams.remove(team)
            comCentral.sendTeamsPairUpdate(team,false)
        }
    }
    private fun logInfo(string: String) { Log.d("TeamRelatedDataModel",string) }

}