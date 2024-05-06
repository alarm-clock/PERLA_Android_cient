/**
 * @file: ComplexServiceStateCallBacks.kt
 * @author: Jozef Michal Bukas <xbukas00@stud.fit.vutbr.cz,jozefmbukas@gmail.com>
 * Description: File containing ComplexServiceStateCallBacks interface
 */
package com.example.jmb_bms.connectionService.in_app_communication

import com.example.jmb_bms.connectionService.models.TeamProfile
import com.example.jmb_bms.connectionService.models.UserProfile
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.CopyOnWriteArraySet

/**
 * Interface with callbacks that are implemented by observes that want to receive user and teams updates. Soon will be DEPRECATED
 * when team updates and complex info receive their own callback interface
 */
interface ComplexServiceStateCallBacks {
    /**
     * Method that sends new list with user profiles
     * @param newList List with [UserProfile]s
     */
    fun updatedUserListCallBack(newList: List<UserProfile>)

    /**
     * Method that sends current location sharing state
     * @param newState New location sharing state
     */
    fun updateSharingLocationState(newState: Boolean)

    /**
     * Method that adds or removes [UserProfile] based on [add] flaf
     * @param profile [UserProfile] that is added or removed
     * @param add Flag indicating if [profile] is added or removed
     */
    fun updateUserList(profile: UserProfile, add: Boolean)

    /**
     * Method that sends updated [UserProfile]
     * @param profile Updated [UserProfile]
     */
    fun profileChanged(profile: UserProfile)

    /**
     * Method that sends clients profile
     * @param profile Clients [UserProfile]
     */
    fun clientProfile(profile: UserProfile)

    /**
     * Method that sends [MutableSet] with all teams info and members
     * @param newSet Set with all teams and their members
     */
    fun setTeamsSet(newSet: MutableSet<Pair<TeamProfile,CopyOnWriteArraySet<UserProfile>>>)

    /**
     * Method that adds or removes [element] based on the [add] flag
     * @param element [Pair] with [TeamProfile] and [MutableSet] with [UserProfile]s that will is added or removed
     * @param add Flag indicating if [element] is added or removed
     */
    fun updateTeamsList(element: Pair<TeamProfile,CopyOnWriteArraySet<UserProfile>>, add: Boolean)

    /**
     * Method that sends updated [TeamProfile]
     * @param element Updated [TeamProfile]
     */
    fun updateTeamsProfile(element: TeamProfile)

    /**
     * Method that adds or removes [UserProfile] in list of team members of team identified by [teamId] based on [add] flag
     * @param teamId ID of team whose member list is updated
     * @param profile [UserProfile] that is added or removed
     * @param add Flag indicating if [profile] is added or removed
     */
    fun updateTeammateList(teamId: String, profile: UserProfile, add: Boolean)

    /**
     * Method that sends all users and teams at same time to prevent race conditions
     * @param newList List of all users
     * @param newSet Set of all teams
     */
    fun setUsersAnTeams(newList: CopyOnWriteArrayList<UserProfile>,newSet: CopyOnWriteArraySet<Pair<TeamProfile,CopyOnWriteArraySet<UserProfile>>>)
}