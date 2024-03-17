package com.example.jmb_bms.connectionService.in_app_communication

import com.example.jmb_bms.connectionService.ConnectionState
import com.example.jmb_bms.connectionService.models.TeamProfile
import com.example.jmb_bms.connectionService.models.UserProfile

class InnerCommunicationCentral {

    var stateCallback: ServiceStateCallback? = null
    var complexServiceStateCallBack: ComplexServiceStateCallBacks? = null

    fun registerStateCallBack(new: ServiceStateCallback) { stateCallback = new }
    fun unRegisterStateCallBack() { stateCallback = null }

    fun sendUpdatedState(newState: ConnectionState){ stateCallback?.onOnServiceStateChanged(newState)}
    fun sendUpdatedErrString(new: String){ stateCallback?.onServiceErroStringChange(new)}

    fun registerComplexCallBack(new: ComplexServiceStateCallBacks){ complexServiceStateCallBack = new}
    fun unRegisterComplexCallBack(){ complexServiceStateCallBack = null}

    fun sendUpdatedUserProfile(profile: UserProfile){ complexServiceStateCallBack?.profileChanged(profile)}

    fun updateLocationSharing(new: Boolean){ complexServiceStateCallBack?.updateSharingLocationState(new)}
    fun sendListUpdate(profile: UserProfile, add: Boolean) { complexServiceStateCallBack?.updateUserList(profile,add)}

    fun sendTeamsPairUpdate(pair: Pair<TeamProfile,HashSet<UserProfile>>, add: Boolean){
        complexServiceStateCallBack?.updateTeamsList(pair,add)
    }
    fun sendUpdatedTeamsProfile(element: TeamProfile){ complexServiceStateCallBack?.updateTeamsProfile(element)}

    fun sendUpdateTeammateList(teamId: String, profile: UserProfile, add: Boolean){
        complexServiceStateCallBack?.updateTeammateList(teamId, profile, add)
    }
    fun sendNewTeamSet(newSet: MutableSet<Pair<TeamProfile,HashSet<UserProfile>>>){
        complexServiceStateCallBack?.setTeamsSet(newSet)
    }
}