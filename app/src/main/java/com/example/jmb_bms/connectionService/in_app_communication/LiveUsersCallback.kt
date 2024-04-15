package com.example.jmb_bms.connectionService.in_app_communication

import com.example.jmb_bms.connectionService.models.UserProfile

interface LiveUsersCallback {
    fun updatedUserListCallBack(newList: List<UserProfile>)
    fun updateUserList(profile: UserProfile, add: Boolean)
    fun profileChanged(profile: UserProfile)
}