/**
 * @file: PointRelatedDataModel.kt
 * @author: Jozef Michal Bukas <xbukas00@stud.fit.vutbr.cz,jozefmbukas@gmail.com>
 * Description: File containing PointRelatedDataModel class
 */
package com.example.jmb_bms.connectionService.models

import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.core.app.NotificationCompat
import androidx.core.content.FileProvider
import com.example.jmb_bms.connectionService.ClientMessage
import com.example.jmb_bms.connectionService.ConnectionService
import com.example.jmb_bms.connectionService.ConnectionState
import com.example.jmb_bms.connectionService.FileSharingRequests
import com.example.jmb_bms.connectionService.in_app_communication.InnerCommunicationCentral
import com.example.jmb_bms.model.database.points.PointDBHelper
import com.example.jmb_bms.model.database.points.PointRow
import com.example.jmb_bms.model.icons.Symbol
import com.example.jmb_bms.model.utils.DownloadResult
import com.example.jmb_bms.model.utils.getOriginalFileName
import com.example.jmb_bms.model.utils.sendNotification
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.websocket.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.SharedFlow
import locus.api.android.ActionDisplayPoints
import locus.api.android.objects.PackPoints
import locus.api.objects.extra.GeoDataExtra
import locus.api.objects.extra.Location
import locus.api.objects.geoData.Point
import locus.api.objects.geoData.addAttachmentPhoto
import locus.api.objects.geoData.parameterDescription
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.CopyOnWriteArrayList

/**
 * Class that implements all point related methods for message parsing
 *
 * @property service [ConnectionService] used to access [ConnectionService.session]
 * @property communicationCentral For observer's callbacks invoking
 */
class PointRelatedDataModel(val service: ConnectionService, val communicationCentral: InnerCommunicationCentral) {

    val downloads = CopyOnWriteArrayList<Pair<Uri, SharedFlow<DownloadResult>>>()

    private fun checkIfUserIsOwner(owner: String?) = (owner == null) || (owner == "Me") || (owner == "All") || (owner == service.serviceModel.profile.serverId)

    fun sendOnlinePoints()
    {
        CoroutineScope(Dispatchers.IO).launch {
            val dbHelper = PointDBHelper(service,null)
            val unsentPoints = dbHelper.getAllUnsentPoints() ?: return@launch
            unsentPoints.forEach {
                sendPoint(it.id)
            }
            dbHelper.close()
        }
    }

    /**
     * Method that checks if file is downloaded
     *
     * @param uri [Uri] referencing checked file
     * @return [SharedFlow] with [DownloadResult] instance
     */
    fun checkIfFileIsDownloaded(uri: Uri): SharedFlow<DownloadResult>?
    {
        return downloads.find { it.first == uri }?.second
    }

    /**
     * Method that parses point creation message received from server
     *
     * @param params Parsed JSON message
     */
    fun parsePointCreationResponse(params: Map<String, Any?>)
    {
        val succ = params["success"] as? Boolean ?: return
        val serverId = params["serverId"] as? String ?: return

        val dbHelper = PointDBHelper(service,null)
        if(succ)
        {
            Log.d("Success",serverId)

            val point = dbHelper.getPointByServerId(serverId) ?: return
            point.postedToServer = true
            point.serverId = serverId
            dbHelper.updatePointIdentById(point)

        } else
        {
            val reason = params["reason"] as? String

            sendNotification(
                service,
                "Failed to upload point",
                "Failed to upload point... try again\nReason: $reason",
                NotificationCompat.PRIORITY_HIGH,
                "3"
            )
            Log.d("Failed",reason!!)
            val point = dbHelper.getPointByServerId(serverId) ?: return
            point.postedToServer = false
            point.serverId = null
            dbHelper.updatePointIdentById(point)
        }
        dbHelper.close()
    }

    /**
     * Method that collects download flow and manages it
     *
     * @param pair [Pair]<Uri to file, Download flow>
     */
    private suspend fun collectDownloadFlow(pair: Pair<Uri, SharedFlow<DownloadResult>>)
    {
        pair.second.collect{
            when(it){
                is DownloadResult.Success -> {
                    downloads.remove(pair)
                }
                is DownloadResult.Error -> {
                    Log.d("DownloadFLowCollect",it.message)
                    downloads.remove(pair)
                    service.contentResolver.delete(pair.first,null,null)
                }
                is  DownloadResult.Progress -> Unit
            }
        }
    }

    /**
     * Method that parses point deletion message received from server
     *
     * @param params Parsed JSON message
     */
    fun parseDeletePoint(params: Map<String, Any?>)
    {
        val serverId = params["serverId"] as? String ?: return
        Log.d("DeletingPointFromServer",serverId)

        val dbHelper = PointDBHelper(service,null)

        val point = dbHelper.getPointByServerId(serverId) ?: return //TODO
        Log.d("DeletingPointFromServer","Found point")
        ActionDisplayPoints.removePackFromLocus(service,point.id.toString())
        point.uris?.forEach {
            service.contentResolver.delete(it,null,null)
        }
        dbHelper.removePoint(point.id)
        communicationCentral.sendDeletedPoint(point.id)
        dbHelper.close()
    }

    /**
     * Method tha downloads all files stored in [list]
     *
     * @param list List with files that must be downloaded
     * @return [List] with downloaded files [Uri]s
     */
    private suspend fun downloadPointFiles(list: List<String>?): List<Uri>?
    {
        if(list.isNullOrEmpty()) return null

        val uriList = mutableListOf<Uri>()

        val requester = FileSharingRequests(service,communicationCentral)

        list.forEach {
            if( File(service.filesDir,it).exists() )
            {
                Log.d("DownloadPointFiles","File already exists")
                val file = File(service.filesDir,it)
                val uri = FileProvider.getUriForFile(service,service.packageName + ".provider", file)
                uriList.add(uri)

            } else
            {
                Log.d("DownloadPointFiles","File does not exists so downloading it from server")
                val file = File(service.filesDir,it)
                val uri = FileProvider.getUriForFile(service,service.packageName + ".provider", file)
                service.grantUriPermission("menion.android.locus",uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)

                uriList.add(uri)

                CoroutineScope(Dispatchers.IO).launch {
                    val downloadFlow = requester.downloadRequest(it,file)
                    val pair = Pair(uri,downloadFlow)
                    downloads.add(pair)

                    collectDownloadFlow(pair)
                }
            }
        }
        return uriList
    }

    /**
     * Method that sends point to Locus
     *
     * @param point Point that will be sent
     */
    private fun addPointToMap(point: PointRow)
    {
        val newPoint = Point("",point.location)

        newPoint.name = point.name
        newPoint.extraData = GeoDataExtra()
        newPoint.extraData!!.addParameter(1,"jmb_bms")
        newPoint.parameterDescription = point.descr

        point.uris?.forEach{
            val res =  newPoint.addAttachmentPhoto(it.toString(),"photo")
            Log.d("RESULT", res.toString())
        }
        newPoint.protected = false
        newPoint.extraData!!.addParameter(2,point.id.toString())
        newPoint.setExtraOnDisplay(
            "com.example.jmb_bms",
            "com.example.jmb_bms.activities.SetAllPointsOnMapActivity",
            "op",
            "e"
        )

        val packPoints = PackPoints(point.id.toString())
        packPoints.bitmap = Symbol(point.symbol,service).imageBitmap?.asAndroidBitmap()
        packPoints.addPoint(newPoint)
        ActionDisplayPoints.sendPackSilent(service,packPoints,false)
    }

    /**
     * Method that compares new set of files and files stored in point and deletes those that are in [point] but aren't
     * in [newFiles] nd downloads those that are in [newFiles] but not in [point]
     *
     * @param newFiles Reference list
     * @param point Stored point
     */
    private suspend fun compareFilesAndManageThem(newFiles: List<String>?, point: PointRow)
    {
        if(newFiles.isNullOrEmpty())
        {
            point.uris?.forEach {
                service.contentResolver.delete(it,null,null)
            }
            point.uris?.clear()
            return
        }
        if(point.uris.isNullOrEmpty())
        {
            point.uris = downloadPointFiles(newFiles)?.toMutableList()
            return
        }

        val files = point.uris?.map { Pair(it,it.getOriginalFileName(service)) }

        val filesToDelete = files?.filterNot { it.second in newFiles }

        if(!filesToDelete.isNullOrEmpty())
        {
            point.uris = point.uris?.filterNot { it in filesToDelete.map { pair -> pair.first } }?.toMutableList()
            filesToDelete.forEach {
                service.contentResolver.delete(it.first,null,null)
            }
        }

        val filesToDownload = newFiles.filterNot { it in files!!.map{ pair -> pair.second} }

        val urisToAdd = downloadPointFiles(filesToDownload) ?: return
        point.uris?.addAll( urisToAdd )

    }

    /**
     * Method that parses point creation message received from server
     *
     * @param params Parsed JSON message
     */
    fun parsePointCreation(params: Map<String, Any?>)
    {
        CoroutineScope(Dispatchers.IO).launch {
            val serverId = params["serverId"] as? String ?: return@launch
            val ownerId = params["ownerId"] as? String ?: return@launch

            if(ownerId == service.serviceModel.profile.serverId) return@launch

            val dbHelper = PointDBHelper(service,null)

            val existingPoint = dbHelper.getPointByServerId(serverId)

            val name = params["name"] as? String ?: return@launch
            val description = params["description"] as? String

            val ownerName = params["ownerName"] as? String ?: return@launch
            val symbol = params["symbol"] as? String ?: return@launch
            val menuString = params["menuString"] as? String ?: return@launch
            val lat = (params["location"] as? Map<String, Any?>)?.get("lat") as? Double ?: return@launch
            val long = (params["location"] as? Map<String, Any?>)?.get("long") as? Double ?: return@launch

            val files = params["files"] as? List<String>

            val uris = if(existingPoint != null) {
                Log.d("FILES",files.toString())
                compareFilesAndManageThem(files,existingPoint)
                existingPoint.uris
            } else {
                downloadPointFiles(files)?.toMutableList()
            }

            val point = PointRow(
                42,
                name,
                true,
                ownerId,
                ownerName,
                serverId,
                true,
                Location(lat,long),
                symbol,
                description ?: "",
                true,
                menuString,
                uris
            )

            if(existingPoint != null)
            {
                point.id = existingPoint.id
                dbHelper.updatePointIdentById(point)
            } else {
                val id = dbHelper.addPoint(point)
                point.id = id
            }
            addPointToMap(point)
            communicationCentral.sendParsedPoint(point.id)
            dbHelper.close()
        }
    }
    suspend fun sendDeletePoint(id: Long): Boolean
    {
        val dbHelper = PointDBHelper(service,null)
        val point = dbHelper.getPoint(id) ?: return false
        if(point.serverId == null)
        {
            Log.d("DeletePointMsg","This point does not not have any serverId")
            return false
        }
        if( !checkIfUserIsOwner(point.ownerId))
        {
            Log.d("DeletePointMsg","This client does not have permission to delete point")
            return false
        }
        service.session.send(Frame.Text(ClientMessage.deletePoint(point.serverId!!)))
        dbHelper.close()
        return true
    }

    /**
     * Method that sends point to server identified by [id]
     *
     * @param id ID of point that will be sent
     */
    suspend fun sendPoint(id: Long)
    {
        val requestMaker = FileSharingRequests(service,communicationCentral)
        val dbHelper = PointDBHelper(service,null)
        val point = dbHelper.getPoint(id)

        if(point == null)
        {
            Log.d("SendPoint","Failed to obtain point from db")
            return
        }

        //transaction id is created here because client can create unique id for whole system
        //it is done by adding user id that is unique for whole system into transaction id
        //then I need just need to make it unique for client and that is done by adding local points id
        //and just to be sure timestamp
        val transactionId = if(point.serverId.isNullOrEmpty())
        {
            "${service.serviceModel.profile.serverId}@${
                SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(
                    Date()
                )}@${id}"
        } else
        {
            point.serverId!!
        }

        if(point.uris != null) {
            val res = requestMaker.sendMultipleFiles(transactionId, point.uris!!){
                sendNotification(
                    service,
                    "Failed to upload point",
                    "Failed to upload file... try again",
                    NotificationCompat.PRIORITY_HIGH,
                    transactionId
                )

                service.session.send(Frame.Text(ClientMessage.failedTransactionAck(transactionId)))
                point.postedToServer = false
                point.serverId = null
                dbHelper.updatePointIdentById(point)
            }
            if(!res) return
        }

        if(service.serviceModel.connectionState == ConnectionState.CONNECTED)
        {
            try {
                point.ownerId = when(point.ownerId) {
                    "Me" -> service.serviceModel.profile.serverId
                    "All" -> "All"
                    else -> service.serviceModel.profile.serverId
                }
                point.serverId = transactionId
                service.session.send(Frame.Text(ClientMessage.createPoint(point,service)))
                point.ownerId = when(point.ownerId){
                    service.serviceModel.profile.serverId -> "Me"
                    "All" -> "All"
                    else -> "Me"
                }
                dbHelper.updatePointIdentById(point)
                dbHelper.close()

            } catch (e: Exception)
            {
                sendNotification(
                    service,
                    "Failed to upload point",
                    "Exception: ${e.printStackTrace()}",
                    NotificationCompat.PRIORITY_HIGH,
                    transactionId
                )
                point.postedToServer = false
                point.serverId = null
                dbHelper.updatePointIdentById(point)
                dbHelper.close()
            }
        } else
        {
            sendNotification(
                service,
                "Failed to upload point",
                "Not connected to server...",
                NotificationCompat.PRIORITY_HIGH,
                transactionId
            )
            point.postedToServer = false
            point.serverId = null
            dbHelper.updatePointIdentById(point)
            dbHelper.close()
        }
    }

    /**
     * Method that sends update point message to server
     *
     * @param id ID of point that was updated
     */
    suspend fun sendUpdatePoint(id: Long)
    {
        Log.d("SendUpdate","In function")
        val dbHelper = PointDBHelper(service,null)
        val point = dbHelper.getPoint(id) ?: return
        dbHelper.close()
        Log.d("SendUpdate","Got point")

        if(!point.online || (point.ownerId != "Me" && point.ownerId != "All")) return

        Log.d("SendUpdate","User can update this point")

        val requestMaker = FileSharingRequests(service,communicationCentral)

        val responses = point.uris?.map {
            CoroutineScope(Dispatchers.IO).async {
                var respone: HttpResponse? = null
                try {
                    respone =  requestMaker.uploadFile(it,true,point.serverId)
                } catch (e:Exception)
                {
                    e.printStackTrace()
                }
                return@async respone
            }
        }?.awaitAll()

        responses?.forEach {
            Log.d("SendPoint","UploadFile response status is: ${it?.status}")
            if( it == null || !it.status.isSuccess())
            {
                sendNotification(
                    service,
                    "Failed to update point",
                    "Failed to upload file... try again",
                    NotificationCompat.PRIORITY_HIGH,
                    point.serverId ?: ""
                )
                service.session.send(Frame.Text(ClientMessage.failedTransactionAck(point.serverId!!)))
                return
            }
        }
        if(service.serviceModel.connectionState == ConnectionState.CONNECTED)
        {
            try {
                Log.d("SendUpdate","Sent message")
                point.ownerId = when(point.ownerId) {
                    "Me" -> service.serviceModel.profile.serverId
                    "All" -> "All"
                    else -> service.serviceModel.profile.serverId
                }
                service.sendMessage(ClientMessage.createPoint(point,service))
                point.ownerId = when(point.ownerId){
                    service.serviceModel.profile.serverId -> "Me"
                    "All" -> "All"
                    else -> "Me"
                }
            } catch (e: Exception)
            {
                sendNotification(service,"Failed to send point update","Exception occurred",NotificationCompat.PRIORITY_HIGH,point.serverId ?: "")
            }
        } else
        {
            sendNotification(
                service,
                "Failed to update point",
                "Not connected to server...",
                NotificationCompat.PRIORITY_HIGH,
                point.serverId ?: ""
            )

        }
    }

    /**
     * Method that sends points sync request to server
     *
     */
    suspend fun sendSync()
    {
        val dbHelper = PointDBHelper(service,null)
        val pointIds = dbHelper.getIdServIdByOwner(true)
        dbHelper.close()

        service.sendMessage(ClientMessage.syncRequest(pointIds.map { it.second }))

        pointIds.forEach {
            sendPoint(it.first)
        }
    }

    /**
     * Method that handles points sync message received from server
     *
     * @param params Parsed JSON message
     */
    fun handleSync(params: Map<String, Any?>)
    {
        val ids = params["ids"] as? List<String> ?: return

        val dbHelper = PointDBHelper(service,null)

        val notOwnedPoints = dbHelper.getIdServIdByOwner(false)

        if(notOwnedPoints.isEmpty()) return

        val pointsForDeletion = notOwnedPoints.filterNot { it.second in ids }

        pointsForDeletion.forEach {
            val point = dbHelper.getPoint(it.first)

            if(point != null)
            {
                ActionDisplayPoints.removePackFromLocus(service,point.id.toString())
                point.uris?.forEach {uri ->
                    service.contentResolver.delete(uri,null,null)
                }
                dbHelper.removePoint(point.id)
                communicationCentral.sendDeletedPoint(point.id)
            }
        }
        dbHelper.close()
    }
}

