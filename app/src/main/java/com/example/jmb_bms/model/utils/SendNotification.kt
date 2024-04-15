    package com.example.jmb_bms.model.utils

import android.Manifest
import android.content.Context
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SmsFailed
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.jmb_bms.R
import java.util.concurrent.atomic.AtomicInteger
object NotificationId{
    private val num = AtomicInteger(0)

    fun getId() = num.incrementAndGet()
}

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