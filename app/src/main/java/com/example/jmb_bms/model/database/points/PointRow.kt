package com.example.jmb_bms.model.database.points

import android.net.Uri
import android.os.Bundle
import locus.api.objects.extra.Location


data class PointRow(
    var id: Long,
    var name: String,
    var online: Boolean,
    var ownerId: String?,
    var ownerName: String?,
    var serverId: String?,
    var postedToServer: Boolean,
    var location: Location,
    var symbol: String,
    var descr: String,
    var visible: Boolean,
    var menuString: String,
    var uris: MutableList<Uri>?
    )
{

    fun putIntoBundle(bundle: Bundle)
    {
        bundle.putLong("id",id)
        bundle.putString("name",name)
        bundle.putString("descr",descr)
        bundle.putString("symbol",symbol)
        bundle.putBoolean("online",online)
        bundle.putBoolean("visible",visible)
        bundle.putDoubleArray("location", doubleArrayOf(location.latitude, location.longitude))

        ownerName?.let {
            bundle.putString("ownerName",it)
        }
        ownerId?.let {
            bundle.putString(ownerId,it)
        }
        uris?.let { list ->
            bundle.putStringArray("uris", Array(list.size){
                list[it].toString()
            })
        }

    }

    companion object{

        fun retrieveFromBundle(bundle: Bundle): PointRow?
        {
            val id = bundle.getLong("id")
            val name = bundle.getString("name") ?: return null
            val descr = bundle.getString("descr") ?: return null
            val symbol = bundle.getString("symbol") ?: return null
            val online = bundle.getBoolean("online")
            val ownerId = bundle.getString("ownerId")
            val ownerName = bundle.getString("ownerName")
            val visible = bundle.getBoolean("visible")

            val locArr = bundle.getDoubleArray("location")!!
            val location = Location(locArr[0], locArr[1])

            val uriList = bundle.getStringArrayList("uris")
            var uris = uriList?.map { Uri.parse(it) }?.toMutableList()

            return PointRow(id,name,online,ownerId,ownerName,name,false,location,symbol,descr,visible,"",uris)
        }
    }

}
