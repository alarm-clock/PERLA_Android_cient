package com.example.jmb_bms

import android.content.SharedPreferences
import androidx.core.content.edit
import com.example.jmb_bms.data.UserIdUserNameTuple
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

fun storeListTupInJsonToSHPref( sharedPreferences: SharedPreferences , list: List<UserIdUserNameTuple>)
{
    val gson = Gson()
    val json = gson.toJson(list)
    sharedPreferences.edit {
        putString("TeamMembersList", json)
        apply()
    }
}

fun getListTupInJsonSHPref(sharedPreferences: SharedPreferences) : List<UserIdUserNameTuple>
{
    val gson = Gson()
    val json = sharedPreferences.getString("TeamMembersList",null)
    val type = object : TypeToken<List<UserIdUserNameTuple>>() {}.type
    return gson.fromJson(json,type)
}