package com.example.jmb_bms.connectionService

interface ComplexServiceStateCallBacks {
    fun updatedUserListCallBack(newList: List<UserProfile>)
    fun updateSharingLocationState(newState: Boolean)

    fun updateUserList(profile: UserProfile, add: Boolean)

    fun profileChanged(profile: UserProfile)
}