/**
 * @file: TeamLiveDataHolder.kt
 * @author: Jozef Michal Bukas <xbukas00@stud.fit.vutbr.cz,jozefmbukas@gmail.com>
 * Description: File containing TeamLiveDataHolder class
 */
package com.example.jmb_bms.model

import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.example.jmb_bms.connectionService.models.TeamProfile
import com.example.jmb_bms.connectionService.models.UserProfile
import kotlinx.coroutines.flow.MutableStateFlow
import java.util.Collections

/**
 * Class that holds all live data for given team. Mainly it holds its [TeamProfile] and mutable set of all users in given team.
 * @param pair Pair with [TeamProfile] and set with user profiles of all users in given team
 * @param list List with live profiles of all users
 * @constructor Creates Live version of [pair] with live profiles of all team members taken from list
 */
class TeamLiveDataHolder(pair: Pair<TeamProfile,MutableSet<UserProfile>>, list: List<MutableLiveData<UserProfile>>){

    //maybe make this private
    val pair: Pair<MutableLiveData<TeamProfile>, MutableStateFlow<MutableSet<MutableLiveData<UserProfile>>>>

    init {
        val newPair = Pair(
            MutableLiveData(pair.first),
            MutableStateFlow( Collections.synchronizedSet( HashSet( pair.second.map { profile ->
                list.find { it.value?.serverId == profile.serverId }  ?: MutableLiveData(profile)
            })))
        )
        this.pair = newPair
    }

    /**
     * Method that updates team profile stored in pair and forces redraw of view
     * @param profile Updated profile
     */
    fun updateTeamProfile(profile: TeamProfile)
    {
        //pair.first.postValue(profile)
        pair.first.value = profile.copy()
    }

    /**
     * Method that adds or removes [UserProfile] from second attribute of pair based on [add] flag.
     * @param element [UserProfile] that will be added or removed
     * @param add Flag indicating if [element] will be added or removed from team members set
     */
    fun updateTeamMembersList(element: UserProfile, add: Boolean)
    {
        val tmp = pair.second.value.toHashSet()

        if(add) tmp.add(MutableLiveData(element))
        else tmp.removeIf { it.value?.serverId == element.serverId }

        pair.second.value = tmp
    }

    /**
     * Method that adds or removes live [UserProfile] from second attribute of pair based on [add] flag.
     * @param element Live [UserProfile] that will be added or removed
     * @param add Flag indicating if [element] will be added or removed from team members set
     */
    fun updateTeamMembersList(element: MutableLiveData<UserProfile>, add: Boolean)
    {
        val tmp = pair.second.value.toHashSet()

        if(add) tmp.add(element)
        else tmp.remove(element)

        pair.second.value = tmp
    }
    fun updateTeamMember(profile: UserProfile)
    {
        pair.second.value.find { it.value?.serverId == profile.serverId }?.value = profile
    }

    /**
     * Method that gets teamId of team stored in instance
     * @return TeamId or null if error occurred
     */
    fun getTeamId(): String? = pair.first.value?._id

    /**
     * Method that gets live [UserProfile] of team leader stored in instance
     * @return Live [UserProfile] of team leader or null if error occurred
     */
    fun getTeamLeader() = pair.second.value.find { it.value?.serverId == pair.first.value?.teamLead }

}