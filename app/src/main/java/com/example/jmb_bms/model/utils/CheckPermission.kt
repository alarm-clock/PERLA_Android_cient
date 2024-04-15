package com.example.jmb_bms.model.utils

import android.content.Context
import android.content.pm.PackageManager

fun checkPermission(context: Context, permission: String): Boolean
{
    val res = context.checkCallingOrSelfPermission(permission)
    return res == PackageManager.PERMISSION_GRANTED
}