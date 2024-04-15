package com.example.jmb_bms.connectionService.in_app_communication

import com.example.jmb_bms.connectionService.models.TeamProfile
import com.example.jmb_bms.connectionService.models.UserProfile
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.CopyOnWriteArraySet

interface ComplexServiceStateCallBacks {
    fun updatedUserListCallBack(newList: List<UserProfile>)
    fun updateSharingLocationState(newState: Boolean)

    fun updateUserList(profile: UserProfile, add: Boolean)

    fun profileChanged(profile: UserProfile)

    fun clientProfile(profile: UserProfile)

    fun setTeamsSet(newSet: MutableSet<Pair<TeamProfile,CopyOnWriteArraySet<UserProfile>>>)

    fun updateTeamsList(element: Pair<TeamProfile,CopyOnWriteArraySet<UserProfile>>, add: Boolean)

    fun updateTeamsProfile(element: TeamProfile)

    fun updateTeammateList(teamId: String, profile: UserProfile, add: Boolean)

    fun setUsersAnTeams(newList: CopyOnWriteArrayList<UserProfile>,newSet: CopyOnWriteArraySet<Pair<TeamProfile,CopyOnWriteArraySet<UserProfile>>>)
}