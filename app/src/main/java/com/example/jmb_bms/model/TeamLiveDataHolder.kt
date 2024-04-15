package com.example.jmb_bms.model

import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.example.jmb_bms.connectionService.models.TeamProfile
import com.example.jmb_bms.connectionService.models.UserProfile
import kotlinx.coroutines.flow.MutableStateFlow
import java.util.Collections

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

    fun updateTeamProfile(profile: TeamProfile)
    {
        //pair.first.postValue(profile)
        pair.first.value = profile.copy()
    }

    fun updateTeamMembersList(element: UserProfile, add: Boolean)
    {
        val tmp = pair.second.value.toHashSet()

        if(add) tmp.add(MutableLiveData(element))
        else tmp.removeIf { it.value?.serverId == element.serverId }

        pair.second.value = tmp
    }

    fun updateTeamMembersList(element: MutableLiveData<UserProfile>, add: Boolean)
    {
        val tmp = pair.second.value.toHashSet()

        if(add) tmp.add(element)
        else Log.d("EEEEEEEEEEEEEEE",tmp.remove(element).toString())

        pair.second.value = tmp
    }
    fun updateTeamMember(profile: UserProfile)
    {
        pair.second.value.find { it.value?.serverId == profile.serverId }?.value = profile
    }

    fun getTeamId(): String? = pair.first.value?._id

    fun getTeamLeader() = pair.second.value.find { it.value?.serverId == pair.first.value?.teamLead }

}