package com.example.jmb_bms.model

import android.content.Context
import com.example.jmb_bms.model.utils.wgs84toMGRS
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn

import locus.api.android.ActionBasics
import locus.api.android.features.periodicUpdates.UpdateContainer
import locus.api.android.objects.LocusVersion
import locus.api.android.utils.LocusUtils


class LocationRepo(private val ctx: Context, val delay: Long = 1000, private val inMGRS: Boolean = true) {


    fun getLocUpdates() : Flow<String>
    {
        return flow {
            //runs as long as flow runs because I want it to run while in menu
            while(true) {
                kotlinx.coroutines.delay(delay)
                var location: String = ""
                LocusUtils.getActiveVersion(ctx)?.let { lv ->
                    ActionBasics.getUpdateContainer(ctx, lv)?.let { uc ->
                        location = handleUpdate(lv, uc)
                    } ?: run {
                        location = handleUpdate(lv, null)
                    }
                } ?: run {
                    location = handleUpdate(null,null)
                }
                emit(location)
            }
        }.flowOn(Dispatchers.IO)
    }

    private fun handleUpdate(lv: LocusVersion?, updateContainer: UpdateContainer?): String
    {
        if(lv == null || updateContainer == null)
        {
            return "No location available"
        }
        return if(inMGRS) wgs84toMGRS(updateContainer.locMyLocation) else "${updateContainer.locMyLocation.latitude} - ${updateContainer.locMyLocation.longitude}"
    }

}