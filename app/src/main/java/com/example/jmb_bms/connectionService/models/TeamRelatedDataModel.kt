package com.example.jmb_bms.connectionService.models

import android.util.Log
import com.example.jmb_bms.connectionService.ClientMessage
import com.example.jmb_bms.connectionService.ConnectionService
import com.example.jmb_bms.connectionService.in_app_communication.InnerCommunicationCentral
import io.ktor.websocket.*
import kotlinx.coroutines.runBlocking
import java.util.*
import kotlin.collections.HashSet
import kotlin.concurrent.thread

class TeamRelatedDataModel(private val comCentral: InnerCommunicationCentral, private val mainModel: ConnectionDataAndState, val service: ConnectionService) {


    private val serviceContext = service
    val teams = Collections.synchronizedSet(hashSetOf<Pair<TeamProfile,HashSet<UserProfile>>>())

    fun findUsersInSameTeam(teamId: String): HashSet<UserProfile>
    {
        val set = HashSet<UserProfile>()
        mainModel.listOfUsers.forEach { profile ->
            logInfo("Client id is ${mainModel.profile.serverId} - this id is ")
            val team = profile.teamEntry.find { it == teamId }

            if(team != null){
                logInfo("In findUsersInSameTeam added user $profile to team")
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

    fun createTeam(params: Map<String, Any?>)
    {
        logInfo("In create team function")
        val teamProfile = TeamProfile.createTeamProfileFromParams(params,serviceContext)
        if(teamProfile == null)
        {
            logInfo("Team profile is null!")
            return
        }
        logInfo("Created team with: id ${teamProfile._id} - name: ${teamProfile.teamName} \n" +
                "   symbol: ${teamProfile.teamIcon} - teamLead: ${teamProfile.teamLead}\n" +
                "   teamEntry: ${teamProfile.teamEntry}")

        val pair = Pair(teamProfile, findUsersInSameTeam(teamProfile._id))


        teams.add(pair)
        comCentral.sendTeamsPairUpdate(pair,true)
        logInfo("Added new team")
    }

    fun deleteTeam(params: Map<String, Any?>)
    {
        logInfo("In deleteTeam function going to delete some team...")
        val id = params["_id"] as? String
        val teamPair = teams.find { it.first._id == id } ?: return
        logInfo("In delete function going to delete team with id $id")

        teams.remove(teamPair)
        comCentral.sendTeamsPairUpdate(teamPair,false)

        logInfo("Removed team in internal structure and sent update to viewModel")

        mainModel.listOfUsers.forEach {
            it.teamEntry.remove(id)
        }
         logInfo("Removed all teamEntries with deleted team in user profiles")
    }

    fun changeTeamLeader(params: Map<String, Any?>)
    {
        val id = params["_id"] as? String ?: return
        val newLeader = params["userId"] as? String ?: return

        val newLeaderProfile = if(mainModel.profile.serverId == newLeader) mainModel.profile else mainModel.listOfUsers.find { it.serverId == newLeader }

        if( newLeaderProfile == null ){
            logInfo("NO user with user Id $newLeader is stored in model")
            return
        }

        val teamProfile = teams.find { it.first._id == id }?.first

        if(teamProfile == null)
        {
            logInfo("No team with Id $id exists in model")
            return
        }
        teamProfile.teamLead = newLeader
        comCentral.sendUpdatedTeamsProfile(teamProfile)

        if( newLeaderProfile.teamEntry.add(id) )
        {
            comCentral.sendUpdatedUserProfile(newLeaderProfile)
            comCentral.sendUpdateTeammateList(id,newLeaderProfile,true)
        }
    }

    fun updateTeam(params: Map<String, Any?>)
    {
        val id = params["_id"] as? String ?: return
        val newName = params["teamName"] as? String ?: return
        val newIcon = params["teamIcon"] as? String ?: return

        val profile = teams.find{ it.first._id == id} ?: return

        profile.first.teamName = newName
        profile.first.teamIcon = newIcon

        comCentral.sendUpdatedTeamsProfile(profile.first)
    }
    fun manageTeamEntry(params: Map<String, Any?>)
    {
        val id = params["_id"] as? String ?: return
        val profileId = params["profileId"] as? String ?: return
        val adding = params["adding"] as? Boolean ?: return

        val userProfile = if(profileId == mainModel.profile.serverId) mainModel.profile else mainModel.listOfUsers.find { it.serverId == profileId } ?: return

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

    fun clearTeams()
    {
        while ( teams.isNotEmpty())
        {
            val team = teams.first()
            teams.remove(team)
            comCentral.sendTeamsPairUpdate(team,false)
        }
    }


    private fun logInfo(string: String) { Log.d("TeamRelatedDataModel",string) }

}