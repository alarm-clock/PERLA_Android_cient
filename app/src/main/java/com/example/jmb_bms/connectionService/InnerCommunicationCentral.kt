package com.example.jmb_bms.connectionService

class InnerCommunicationCentral {

    var stateCallback: ServiceStateCallback? = null
    var complexServiceStateCallBack: ComplexServiceStateCallBacks? = null

    fun registerStateCallBack(new: ServiceStateCallback) { stateCallback = new }
    fun unRegisterStateCallBack() { stateCallback = null }

    fun sendUpdatedState(newState: ConnectionState){ stateCallback?.onOnServiceStateChanged(newState)}
    fun sendUpdatedErrString(new: String){ stateCallback?.onServiceErroStringChange(new)}

    fun registerComplexCallBack(new: ComplexServiceStateCallBacks){ complexServiceStateCallBack = new}
    fun unRegisterComplexCallBack(){ complexServiceStateCallBack = null}

    fun updateLocationSharing(new: Boolean){ complexServiceStateCallBack?.updateSharingLocationState(new)}
    fun sendListUpdate(profile: UserProfile, add: Boolean) { complexServiceStateCallBack?.updateUserList(profile,add)}
}