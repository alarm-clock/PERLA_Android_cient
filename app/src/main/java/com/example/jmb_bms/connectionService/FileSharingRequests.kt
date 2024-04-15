package com.example.jmb_bms.connectionService

import android.net.Uri
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.net.toFile
import com.example.jmb_bms.connectionService.in_app_communication.InnerCommunicationCentral
import com.example.jmb_bms.connectionService.models.UserProfile
import com.example.jmb_bms.model.utils.DownloadResult
import com.example.jmb_bms.model.utils.getFileSize
import com.example.jmb_bms.model.utils.getOriginalFileName
import com.example.jmb_bms.model.utils.sendNotification
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.android.*
import io.ktor.client.request.*
import io.ktor.client.request.forms.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.util.*
import io.ktor.utils.io.*
import io.ktor.utils.io.core.*
import io.ktor.websocket.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import java.io.File
import kotlin.math.roundToInt


class FileSharingRequests(val connectionService: ConnectionService, val comCentral: InnerCommunicationCentral) {

    fun getClient() = HttpClient(Android)

    @OptIn(InternalAPI::class)
    suspend fun uploadFile(uri: Uri, pointData: Boolean, transactionId: String? = null): HttpResponse{

        //val file = // this is where I need to open a file
        val fileBytes = connectionService.contentResolver.openInputStream(uri)?.buffered()?.use { it.readBytes() } ?: throw Exception()  ///file.readBytes()
        val fileName = uri.getOriginalFileName(connectionService)
        val mimeType = "application/octet-stream"

        val client = getClient()

        return client.post("http://${connectionService.serviceModel.host}:${connectionService.serviceModel.port}/upload") {
            headers {
                append("SESSION", connectionService.serviceModel.profile.serverId)
            }
            body = MultiPartFormDataContent( formData {
                append(
                    key = "point",
                    value = pointData
                )
                append(
                    key = "transactionId",
                    value = transactionId ?: ""
                )
                appendInput(
                    key = "file",
                    headers = Headers.build {
                        append(HttpHeaders.ContentDisposition, "filename=$fileName")
                        append(HttpHeaders.ContentType, mimeType)
                    },
                    size = uri.getFileSize(connectionService.contentResolver),
                    block = {
                        buildPacket {
                            writeFully(fileBytes)
                        }
                    }
                )
            })
        }
    }

    @OptIn(InternalAPI::class)
    suspend fun downloadRequest(serverFileName: String, file: File ): SharedFlow<DownloadResult>
    {
        val shFlow = MutableSharedFlow<DownloadResult>(replay = 0, extraBufferCapacity = 3)

        CoroutineScope(Dispatchers.IO).launch {
            try {
                getClient().prepareGet("http://${connectionService.serviceModel.host}:${connectionService.serviceModel.port}/download/$serverFileName"){
                    headers{
                        append("SESSION", connectionService.serviceModel.profile.serverId)
                    }
                }.execute { response ->
                    val channel: ByteReadChannel = response.body()
                    while (!channel.isClosedForRead){

                        val packet = channel.readRemaining(DEFAULT_BUFFER_SIZE.toLong())
                        while (!packet.isEmpty)
                        {
                            val bytes = packet.readBytes()
                            file.appendBytes(bytes)
                            val progress = file.length() * 100f / response.contentLength()!!
                            shFlow.emit(DownloadResult.Progress(progress.roundToInt()))
                        }
                    }
                    if(response.status.isSuccess())
                    {
                        shFlow.emit(DownloadResult.Success)
                    } else
                    {
                        shFlow.emit(DownloadResult.Error("Failed to download image with status code: ${response.status}"))
                    }
                }
            } catch (e: Exception)
            {
                shFlow.emit(DownloadResult.Error("Exception occurred",e))
            }
        }
        return shFlow.asSharedFlow()
    }


    suspend fun sendMultipleFiles(transactionId: String?, files: List<Uri>, onFail: suspend () -> Unit): Boolean
    {
        val responses = files.map {
            CoroutineScope(Dispatchers.IO).async {
                var respone: HttpResponse? = null
                try {
                    respone = uploadFile(it,true,transactionId)
                } catch (e:Exception)
                {
                    e.printStackTrace()
                }
                return@async respone
            }
        }.awaitAll()

        responses.forEach {
            Log.d("SendPoint","UploadFile response status is: ${it?.status}")
            if( it == null || !it.status.isSuccess())
            {
                onFail()
                return false
            }
        }

        return true
    }

}