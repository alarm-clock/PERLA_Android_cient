/**
 * @file: PointRow.kt
 * @author: Jozef Michal Bukas <xbukas00@stud.fit.vutbr.cz,jozefmbukas@gmail.com>
 * Description: File containing PointRow class
 */
package com.example.jmb_bms.model.database.points

import android.net.Uri
import android.os.Bundle
import locus.api.objects.extra.Location

/**
 * Data class for one row in points database.
 * @param id Points id in clients database, unique only for current client
 * @param name Points name
 * @param online Flag indicating if point is online point
 * @param ownerId Server id of owner (user that can update point) or All if everyone can update point or Me if current
 * client can update point
 * @param ownerName Server name of user
 * @param serverId Global points id unique for all users
 * @param postedToServer Flag indicating if point was sent to server
 * @param location [Location] instance with points location on map
 * @param symbol Symbol code in 2525
 * @param descr Points description
 * @param visible Flag indicating if point is visible on map
 * @param menuString String used to initialize symbol creation menu when updating point
 * @param uris [MutableList]<[Uri]> with all attached files
 */
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

    /**
     * Method for putting point into bundle
     * @param bundle [Bundle] into which point is stored
     */
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

        /**
         * Method that returns initialized [PointRow] instance from [bundle]
         * @param bundle [Bundle] containing point
         * @return [PointRow] or null if error occurred
         */
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
