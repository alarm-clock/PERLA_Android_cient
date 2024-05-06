/**
 * @file: LiveUsersCallback.kt
 * @author: Jozef Michal Bukas <xbukas00@stud.fit.vutbr.cz,jozefmbukas@gmail.com>
 * Description: File containing LiveUsersCallback interface
 */
package com.example.jmb_bms.connectionService.in_app_communication

import com.example.jmb_bms.connectionService.models.UserProfile

/**
 * Interface with callback methods that are implemented by observers that want user updates
 */
interface LiveUsersCallback {
    /**
     * Method that sends new list with user profiles
     * @param newList List with [UserProfile]s
     */
    fun updatedUserListCallBack(newList: List<UserProfile>)

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
}