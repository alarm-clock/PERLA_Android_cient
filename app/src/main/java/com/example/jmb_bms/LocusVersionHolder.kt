package com.example.jmb_bms

import android.content.Context
import locus.api.android.ActionBasics
import locus.api.android.objects.LocusVersion
import locus.api.android.utils.LocusUtils

object LocusVersionHolder {

    private var locusVersion: LocusVersion? = null

    fun checkAndSotreLocVer(ctx: Context)
    {
        locusVersion = LocusUtils.getActiveVersion(ctx)

    }
    fun getLv(): LocusVersion? = locusVersion

    fun getLvWithStore(ctx: Context): LocusVersion?{
        checkAndSotreLocVer(ctx)
        return getLv()
    }

    fun getLvNotNull(): LocusVersion = locusVersion!!

}