/**
 * @file: InnerCommunicationCentral.kt
 * @author: Jozef Michal Bukas <xbukas00@stud.fit.vutbr.cz,jozefmbukas@gmail.com>
 * Description: File containing InnerCommunicationCentral class
 */
package com.example.jmb_bms.connectionService.in_app_communication

import com.example.jmb_bms.connectionService.ConnectionState
import com.example.jmb_bms.connectionService.models.TeamProfile
import com.example.jmb_bms.connectionService.models.UserProfile
import com.example.jmb_bms.model.ChatMessage
import java.util.concurrent.CopyOnWriteArraySet

/**
 * Class that hold references to all observers is responsible for relaying new data if there is someone who listens for updates.
 * This class can hold only one observer of any type because there can be only one observer at a time anyway. It servers
 * as inner communication manager for Connection Service
 */
class InnerCommunicationCentral {

    var stateCallback: ServiceStateCallback? = null
    var complexServiceStateCallBack: ComplexServiceStateCallBacks? = null
    var pointRelatedCallBacks: PointRelatedCallBacks? = null
    var liveUsersCallback: LiveUsersCallback? = null
    var chatRoomsCallBack: ChatRoomsCallBacks? = null

    /**
     * Method for registering [PointRelatedCallBacks] observer
     * @param new New observer
     */
    fun registerPointRelatedCallBacks(new: PointRelatedCallBacks){ pointRelatedCallBacks = new }

    /**
     * Method that unregisters reference of [PointRelatedCallBacks] observer
     */
    fun unregisterPointRelatedCallBacks(){ pointRelatedCallBacks = null }

    /**
     * Method for registering [ServiceStateCallback] observer
     * @param new New observer
     */
    fun registerStateCallBack(new: ServiceStateCallback) { stateCallback = new }

    /**
     * Method that unregisters reference of [ServiceStateCallback] observer
     */
    fun unRegisterStateCallBack() { stateCallback = null }

    /**
     * Method that invokes [ServiceStateCallback.onOnServiceStateChanged] callback of observer if there is any
     * @param newState New connection state
     */
    fun sendUpdatedState(newState: ConnectionState){ stateCallback?.onOnServiceStateChanged(newState)}

    /**
     * Method that invokes [ServiceStateCallback.onServiceErroStringChange] callback of observer if there is any
     * @param new New error string
     */
    fun sendUpdatedErrString(new: String){ stateCallback?.onServiceErroStringChange(new)}

    /**
     * Method for registering [ComplexServiceStateCallBacks] observer
     * @param new New observer
     */
    fun registerComplexCallBack(new: ComplexServiceStateCallBacks){ complexServiceStateCallBack = new}

    /**
     * Method that unregisters reference of [ComplexServiceStateCallBacks] observer
     */
    fun unRegisterComplexCallBack(){ complexServiceStateCallBack = null}

    /**
     * Method for registering [LiveUsersCallback] observer
     * @param new New observer
     */
    fun registerLiveUsersCallback(new: LiveUsersCallback){ liveUsersCallback = new}

    /**
     * Method that unregisters reference of [LiveUsersCallback] observer
     */
    fun unregisterLiveUsersCallback(){ liveUsersCallback = null}

    /**
     * Method for registering [ChatRoomsCallBacks] observer
     * @param new New observer
     */
    fun registerChatRoomsCallback(new: ChatRoomsCallBacks){ chatRoomsCallBack = new }

    /**
     * Method that unregisters reference of [ChatRoomsCallBacks] observer
     */
    fun unregisterChatRoomsCallback(){ chatRoomsCallBack = null }

    /**
     * Method that invokes [ChatRoomsCallBacks.manageRooms] method in observer if there is any
     */
    fun sendChatRoomsUpdate(id: String, add: Boolean){
         chatRoomsCallBack?.manageRooms(id, add)
    }

    /**
     * Method that invokes [ChatRoomsCallBacks.parseMessage] method in observer if there is any
     */
    fun sendChatMessage(message: ChatMessage)
    {
        chatRoomsCallBack?.parseMessage(message)
    }

    /**
     * Method that invokes [ChatRoomsCallBacks.parseMultipleMessages] method in observer if there is any
     */
    fun sendMultipleChatMessages(messages: List<ChatMessage>)
    {
        chatRoomsCallBack?.parseMultipleMessages(messages,messages.first().chatRoomId)
    }

    /**
     * Method that invokes [ComplexServiceStateCallBacks.profileChanged] and [LiveUsersCallback.profileChanged] methods in observer if there is any
     */
    fun sendUpdatedUserProfile(profile: UserProfile){
        complexServiceStateCallBack?.profileChanged(profile)
        liveUsersCallback?.profileChanged(profile)
    }

    /**
     * Method that invokes [ComplexServiceStateCallBacks.updateSharingLocationState] method in observer if there is any
     */
    fun updateLocationSharing(new: Boolean){ complexServiceStateCallBack?.updateSharingLocationState(new)}

    /**
     * Method that invokes [ComplexServiceStateCallBacks.updateUserList] and [LiveUsersCallback.updateUserList] methods in observer if there is any
     */
    fun sendListUpdate(profile: UserProfile, add: Boolean) {
        complexServiceStateCallBack?.updateUserList(profile,add)
        liveUsersCallback?.updateUserList(profile, add)
    }

    /**
     * Method that invokes [ComplexServiceStateCallBacks.updateTeamsList] method in observer if there is any
     */
    fun sendTeamsPairUpdate(pair: Pair<TeamProfile,CopyOnWriteArraySet<UserProfile>>, add: Boolean){
        complexServiceStateCallBack?.updateTeamsList(pair,add)
    }

    /**
     * Method that invokes [ComplexServiceStateCallBacks.updateTeamsProfile] method in observer if there is any
     */
    fun sendUpdatedTeamsProfile(element: TeamProfile){ complexServiceStateCallBack?.updateTeamsProfile(element)}

    /**
     * Method that invokes [ComplexServiceStateCallBacks.updateTeammateList] method in observer if there is any
     */
    fun sendUpdateTeammateList(teamId: String, profile: UserProfile, add: Boolean){
        complexServiceStateCallBack?.updateTeammateList(teamId, profile, add)
    }

    /**
     * Method that invokes [ComplexServiceStateCallBacks.setTeamsSet] method in observer if there is any
     */
    fun sendNewTeamSet(newSet: MutableSet<Pair<TeamProfile,CopyOnWriteArraySet<UserProfile>>>){
        complexServiceStateCallBack?.setTeamsSet(newSet)
    }

    /**
     * Method that invokes [PointRelatedCallBacks.parsedPoint] method in observer if there is any
     */
    fun sendParsedPoint(id: Long)
    {
        pointRelatedCallBacks?.parsedPoint(id)
    }

    /**
     * Method that invokes [PointRelatedCallBacks.deletedPoint] method in observer if there is any
     */
    fun sendDeletedPoint(id: Long)
    {
        pointRelatedCallBacks?.deletedPoint(id)
    }
}