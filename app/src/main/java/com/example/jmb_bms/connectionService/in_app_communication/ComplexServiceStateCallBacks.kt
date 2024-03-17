package com.example.jmb_bms.connectionService.in_app_communication

import com.example.jmb_bms.connectionService.models.TeamProfile
import com.example.jmb_bms.connectionService.models.UserProfile

interface ComplexServiceStateCallBacks {
    fun updatedUserListCallBack(newList: List<UserProfile>)
    fun updateSharingLocationState(newState: Boolean)

    fun updateUserList(profile: UserProfile, add: Boolean)

    fun profileChanged(profile: UserProfile)

    fun clientProfile(profile: UserProfile)

    fun setTeamsSet(newSet: MutableSet<Pair<TeamProfile,HashSet<UserProfile>>>)

    fun updateTeamsList(element: Pair<TeamProfile,HashSet<UserProfile>>, add: Boolean)

    fun updateTeamsProfile(element: TeamProfile)

    fun updateTeammateList(teamId: String, profile: UserProfile, add: Boolean)

    fun setUsersAnTeams(newList: List<UserProfile>,newSet: MutableSet<Pair<TeamProfile,HashSet<UserProfile>>>)
}