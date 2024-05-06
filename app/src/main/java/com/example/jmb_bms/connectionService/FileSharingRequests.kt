/**
 * @file: FileSharingRequests.kt
 * @author: Jozef Michal Bukas <xbukas00@stud.fit.vutbr.cz,jozefmbukas@gmail.com>
 * Description: File containing FileSharingRequests class
 */
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
import io.ktor.client.engine.cio.*
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
import java.security.cert.X509Certificate
import javax.net.ssl.X509TrustManager
import kotlin.math.roundToInt

/**
 * Class that has methods for sending files to server or getting them from server. Classic HTTP POST and GET methods are used.
 * @param connectionService Reference to connection service to access its model and use it as context
 */
class FileSharingRequests(val connectionService: ConnectionService, val comCentral: InnerCommunicationCentral) {

    /**
     * Method that creates Http client that can be used to communicate with server. Note that this client doesn't check
     * certificate validity and will accept any certificate. But most of the time ip address is used to connect to
     * server so dns is used where we could get fake dns entry. It is also easier for testing to use self-signed certificates
     * when running server on my computer. In the future I will add option in settings to check/ not check server certificates
     */
    fun getClient() = HttpClient(CIO){
        engine{
            https {
                if(connectionService.testing)
                {
                    val trustAllCerts = object : X509TrustManager {
                        override fun checkClientTrusted(chain: Array<out X509Certificate>?, authType: String?) {}
                        override fun checkServerTrusted(chain: Array<out X509Certificate>?, authType: String?) {}
                        override fun getAcceptedIssuers(): Array<X509Certificate> = emptyArray()
                    }
                    trustManager = trustAllCerts
                }
            }
        }
    }

    /**
     * Method for uploading file to server. Note that you must have active websocket connection to upload file.
     * @param uri Uploaded file [Uri]
     * @param pointData Flag indicating that this file upload is part of larger upload
     * @param transactionId Transaction ID used to add uploaded file to larger transaction
     * @return [HttpResponse]
     */
    @OptIn(InternalAPI::class)
    suspend fun uploadFile(uri: Uri, pointData: Boolean, transactionId: String? = null): HttpResponse{

        //val file = // this is where I need to open a file
        val fileBytes = connectionService.contentResolver.openInputStream(uri)?.buffered()?.use { it.readBytes() } ?: throw Exception()  ///file.readBytes()
        val fileName = uri.getOriginalFileName(connectionService)
        val mimeType = "application/octet-stream"

        val client = getClient()

        return client.post("https://${connectionService.serviceModel.host}:${connectionService.serviceModel.port}/upload") {
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

    /**
     * Method for downloading file from server. Note that you must have active websocket connection to do so.
     * @param serverFileName File name
     * @param file [File] into which downloaded file will be stored
     * @return [SharedFlow]<[DownloadResult]> In which file is downloaded and download status is emitted.
     */
    suspend fun downloadRequest(serverFileName: String, file: File ): SharedFlow<DownloadResult>
    {
        //last three emits are emitted for new collector, it can not be replayed
        val shFlow = MutableSharedFlow<DownloadResult>(replay = 0, extraBufferCapacity = 3)

        CoroutineScope(Dispatchers.IO).launch {
            try {
                getClient().prepareGet("https://${connectionService.serviceModel.host}:${connectionService.serviceModel.port}/download/$serverFileName"){
                    headers{
                        append("SESSION", connectionService.serviceModel.profile.serverId)
                    }
                }.execute { response ->
                    val channel: ByteReadChannel = response.body()
                    while (!channel.isClosedForRead){

                        val packet = channel.readRemaining(DEFAULT_BUFFER_SIZE.toLong()) //get one packet from channel and read it
                        while (!packet.isEmpty)
                        {
                            val bytes = packet.readBytes()
                            file.appendBytes(bytes)
                            val progress = file.length() * 100f / response.contentLength()!! //calculate download percentage
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

    /**
     * Method for sending multiple files at once. Note that you must have active websocket connection to do so.
     * @param transactionId Transaction ID used to add uploaded file to larger transaction. If they are separate upload pass null
     * @param files [List]<[Uri]> with all files that are going to be uploaded
     * @param onFail Closure that is invoked when upload fails
     * @return True if all files were uploaded else false
     */
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

        //check all responses, if even one failed return false
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