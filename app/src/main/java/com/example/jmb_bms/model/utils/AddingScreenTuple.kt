package com.example.jmb_bms.model.utils

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.MutableLiveData
import com.example.jmb_bms.connectionService.models.UserProfile

data class AddingScreenTuple(
    var profile: MutableLiveData<UserProfile>,
    var selected: MutableState<Boolean> = mutableStateOf(false )
)