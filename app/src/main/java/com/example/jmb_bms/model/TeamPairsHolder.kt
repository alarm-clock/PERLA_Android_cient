/**
 * @file: TeamPairsHolder.kt
 * @author: Jozef Michal Bukas <xbukas00@stud.fit.vutbr.cz,jozefmbukas@gmail.com>
 * Description: File containing TeamPairsHolder class
 */
package com.example.jmb_bms.model


import androidx.lifecycle.MutableLiveData
import com.example.jmb_bms.connectionService.models.UserProfile
import kotlinx.coroutines.flow.MutableStateFlow
import java.util.*
import kotlin.collections.HashSet

/**
 * Class that holds all teams live data in hashset and implements few methods for working with given data structure
 * @param data [MutableStateFlow] with hashset for [TeamLiveDataHolder]
 * */
class TeamPairsHolder(val data: MutableStateFlow<MutableSet<TeamLiveDataHolder>> = MutableStateFlow(Collections.synchronizedSet(HashSet<TeamLiveDataHolder>()))) {

    /**
     * Method that adds or removes [TeamLiveDataHolder] from [data] based on [add] parameter
     * @param data [TeamLiveDataHolder] which will be added/deleted
     * @param add Flag indicating if [data] will be added or deleted
     */
    fun updateSet(data: TeamLiveDataHolder, add: Boolean)
    {
        val tmp = this.data.value.toHashSet()

        if(add) tmp.add(data)
        else tmp.removeIf{ it.getTeamId() == data.getTeamId()}

        this.data.value = tmp
    }

    /**
     * Method that sets brand new mutable set to [data] attribute
     * @param newSet New mutable set that will replace current set in [data]
     */
    fun setNewSet(newSet: MutableSet<TeamLiveDataHolder>)
    {
        data.value = newSet
    }

    /**
     * Method that finds [TeamLiveDataHolder] based on the the [teamId]
     * @param teamId ID of team that is searched
     * @return [TeamLiveDataHolder] with same [teamId] or null of no team with given [teamId] exists
     */
    fun findPair(teamId: String): TeamLiveDataHolder? = data.value.find { it.getTeamId() == teamId }

    fun findExistingLiveUserProfile(profileId: String): MutableLiveData<UserProfile>?
    {
        val set = data.value
        set.forEach {
            val ret = it.pair.second.value.find { liveProfile -> liveProfile.value?.serverId == profileId }
            if(ret != null) return ret
        }
        return null
    }
}