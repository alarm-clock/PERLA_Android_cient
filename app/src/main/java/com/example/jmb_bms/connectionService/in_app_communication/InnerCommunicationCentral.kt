package com.example.jmb_bms.connectionService.in_app_communication

import com.example.jmb_bms.connectionService.ConnectionState
import com.example.jmb_bms.connectionService.models.TeamProfile
import com.example.jmb_bms.connectionService.models.UserProfile
import com.example.jmb_bms.model.ChatMessage
import java.util.concurrent.CopyOnWriteArraySet

class InnerCommunicationCentral {

    var stateCallback: ServiceStateCallback? = null
    var complexServiceStateCallBack: ComplexServiceStateCallBacks? = null
    var pointRelatedCallBacks: PointRelatedCallBacks? = null
    var liveUsersCallback: LiveUsersCallback? = null
    var chatRoomsCallBack: ChatRoomsCallBacks? = null

    fun registerPointRelatedCallBacks(new: PointRelatedCallBacks){ pointRelatedCallBacks = new }
    fun unregisterPointRelatedCallBacks(){ pointRelatedCallBacks = null }

    fun registerStateCallBack(new: ServiceStateCallback) { stateCallback = new }
    fun unRegisterStateCallBack() { stateCallback = null }

    fun sendUpdatedState(newState: ConnectionState){ stateCallback?.onOnServiceStateChanged(newState)}
    fun sendUpdatedErrString(new: String){ stateCallback?.onServiceErroStringChange(new)}

    fun registerComplexCallBack(new: ComplexServiceStateCallBacks){ complexServiceStateCallBack = new}
    fun unRegisterComplexCallBack(){ complexServiceStateCallBack = null}

    fun registerLiveUsersCallback(new: LiveUsersCallback){ liveUsersCallback = new}
    fun unregisterLiveUsersCallback(){ liveUsersCallback = null}

    fun registerChatRoomsCallback(new: ChatRoomsCallBacks){ chatRoomsCallBack = new }
    fun unregisterChatRoomsCallback(){ chatRoomsCallBack = null }

    fun sendChatRoomsUpdate(id: String, add: Boolean){
         chatRoomsCallBack?.manageRooms(id, add)
    }

    fun sendChatMessage(message: ChatMessage)
    {
        chatRoomsCallBack?.parseMessage(message)
    }

    fun sendMultipleChatMessages(messages: List<ChatMessage>)
    {
        chatRoomsCallBack?.parseMultipleMessages(messages,messages.first().chatRoomId)
    }

    fun sendUpdatedUserProfile(profile: UserProfile){
        complexServiceStateCallBack?.profileChanged(profile)
        liveUsersCallback?.profileChanged(profile)
    }

    fun updateLocationSharing(new: Boolean){ complexServiceStateCallBack?.updateSharingLocationState(new)}
    fun sendListUpdate(profile: UserProfile, add: Boolean) {
        complexServiceStateCallBack?.updateUserList(profile,add)
        liveUsersCallback?.updateUserList(profile, add)
    }

    fun sendTeamsPairUpdate(pair: Pair<TeamProfile,CopyOnWriteArraySet<UserProfile>>, add: Boolean){
        complexServiceStateCallBack?.updateTeamsList(pair,add)
    }
    fun sendUpdatedTeamsProfile(element: TeamProfile){ complexServiceStateCallBack?.updateTeamsProfile(element)}

    fun sendUpdateTeammateList(teamId: String, profile: UserProfile, add: Boolean){
        complexServiceStateCallBack?.updateTeammateList(teamId, profile, add)
    }
    fun sendNewTeamSet(newSet: MutableSet<Pair<TeamProfile,CopyOnWriteArraySet<UserProfile>>>){
        complexServiceStateCallBack?.setTeamsSet(newSet)
    }

    fun sendParsedPoint(id: Long)
    {
        pointRelatedCallBacks?.parsedPoint(id)
    }
    fun sendDeletedPoint(id: Long)
    {
        pointRelatedCallBacks?.deletedPoint(id)
    }
}