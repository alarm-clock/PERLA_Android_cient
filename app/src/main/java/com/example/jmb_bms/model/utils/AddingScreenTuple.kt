/**
 * @file: AddingScreenTuple.kt
 * @author: Jozef Michal Bukas <xbukas00@stud.fit.vutbr.cz,jozefmbukas@gmail.com>
 * Description: File containing AddingScreenTuple class
 */
package com.example.jmb_bms.model.utils

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.MutableLiveData
import com.example.jmb_bms.connectionService.models.UserProfile

/**
 * Data class that holds data for screens where users are marked or put into some list
 * @param profile Live [UserProfile]
 * @param selected [MutableState]<[Boolean]> representing if user is picked or not
 */
data class AddingScreenTuple(
    var profile: MutableLiveData<UserProfile>,
    var selected: MutableState<Boolean> = mutableStateOf(false )
)