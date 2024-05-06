/**
 * @file: CheckPermission.kt
 * @author: Jozef Michal Bukas <xbukas00@stud.fit.vutbr.cz,jozefmbukas@gmail.com>
 * Description: File containing function that check permission
 */
package com.example.jmb_bms.model.utils

import android.content.Context
import android.content.pm.PackageManager

/**
 * Function that checks if application was given [permission]
 * @param context Context used to check permission
 * @param permission String with permission
 * @return True if permission was granted else false
 */
fun checkPermission(context: Context, permission: String): Boolean
{
    val res = context.checkCallingOrSelfPermission(permission)
    return res == PackageManager.PERMISSION_GRANTED
}