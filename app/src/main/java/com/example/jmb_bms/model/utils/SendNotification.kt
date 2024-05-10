/**
 * @file: SendNotification.kt
 * @author: Jozef Michal Bukas <xbukas00@stud.fit.vutbr.cz,jozefmbukas@gmail.com>
 * Description: File containing NotificationId object and function sendNotification
 */
package com.example.jmb_bms.model.utils

import android.Manifest
import android.content.Context
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.jmb_bms.R
import java.util.concurrent.atomic.AtomicInteger

/**
 * Object that servers as atomic counter for notification id
 */
object NotificationId{
    private val num = AtomicInteger(0)

    /**
     * Method that returns notification id and increments [num]
     */
    fun getId() = num.incrementAndGet()
}

/**
 * Method that sends notification to system into chanel with id jmb_bms_1
 * @param ctx Context used to check permission and build notification
 * @param title Notification title
 * @param body Notification body
 * @param priority Notification priority
 */
fun sendNotification(ctx: Context,title: String, body: String, priority: Int, id: String )
{
    if(checkPermission(ctx, Manifest.permission.POST_NOTIFICATIONS))
    {
        val notification =  NotificationCompat.Builder(ctx,"jmb_bms_1")
            .setContentTitle(title)
            .setContentText(body)
            .setPriority(priority)
            .setSmallIcon(R.drawable.bms_working_icon)
            .build()

        with(NotificationManagerCompat.from(ctx)){
            notify(NotificationId.getId(),notification)
        }
    }
}