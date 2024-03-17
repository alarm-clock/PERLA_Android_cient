package com.example.jmb_bms.model

import android.security.identity.AccessControlProfileId
import androidx.lifecycle.MutableLiveData
import com.example.jmb_bms.connectionService.models.UserProfile
import kotlinx.coroutines.flow.MutableStateFlow
import java.util.*
import kotlin.collections.HashSet

class TeamPairsHolder(val data: MutableStateFlow<MutableSet<TeamLiveDataHolder>> = MutableStateFlow(Collections.synchronizedSet(HashSet<TeamLiveDataHolder>()))) {

    fun updateSet(data: TeamLiveDataHolder, add: Boolean)
    {
        if(add) this.data.value.add(data)
        else this.data.value.remove(data)
    }

    fun setNewSet(newSet: MutableSet<TeamLiveDataHolder>)
    {
        data.value = newSet
    }

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