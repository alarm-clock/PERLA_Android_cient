package com.example.jmb_bms.viewModel.point

import android.annotation.SuppressLint
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.IBinder
import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.graphics.ImageBitmap
import androidx.lifecycle.*
import com.example.jmb_bms.connectionService.ConnectionService
import com.example.jmb_bms.connectionService.ConnectionState
import com.example.jmb_bms.connectionService.in_app_communication.PointRelatedCallBacks
import com.example.jmb_bms.connectionService.in_app_communication.ServiceStateCallback
import com.example.jmb_bms.model.database.points.PointDBHelper
import com.example.jmb_bms.model.database.points.PointRow
import com.example.jmb_bms.model.icons.Symbol
import com.example.jmb_bms.model.utils.DownloadResult
import com.example.jmb_bms.model.utils.MimeTypes
import com.example.jmb_bms.model.utils.PointDetailFileHolder
import com.example.jmb_bms.model.utils.runOnThread
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import locus.api.android.ActionDisplayPoints
import locus.api.objects.geoData.Point

class PointDetailVM(private val dbHelper: PointDBHelper, @SuppressLint("StaticFieldLeak") val appCtx: Context) : ViewModel(), ServiceStateCallback, PointRelatedCallBacks {

    private var service : ConnectionService? = null

    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, serviceBin: IBinder?) {
            val binder = serviceBin as ConnectionService.LocalBinder
            service = binder.getService()

            Log.d("PointCreationVM","Setting myself as callback to $service")
            service?.setCallBack(this@PointDetailVM)
            service?.setPointCallBacks(this@PointDetailVM)
            //service?.setComplexDataCallBack(this@ServerVM)
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            service = null
        }
    }

    init {
        val running = appCtx.getSharedPreferences("jmb_bms_Server_Info", Context.MODE_PRIVATE).getBoolean("Service_Running",false)
        if(running) bind()
    }

    private val _connectionState = MutableLiveData<ConnectionState>(ConnectionState.NONE)
    val connectionState: LiveData<ConnectionState> = _connectionState

    private val _connectionErrorMsg = MutableLiveData("")
    val connectionErrorMsg: LiveData<String> = _connectionErrorMsg


    lateinit var pointRow : PointRow

    private lateinit var symbol : Symbol

    val liveSymbol = MutableLiveData<ImageBitmap?>(null)
    val pointName = MutableLiveData("")
    val pointDescription = MutableLiveData("")
    val ownerName = MutableLiveData("")
    val ownerState = MutableLiveData(false)
    val online = MutableLiveData(false)
    val loading = MutableLiveData(true)
    val uris = mutableStateOf(listOf<PointDetailFileHolder>())
    val thumbnails = mutableListOf<Pair<Uri, Bitmap>>()

    val deleted = MutableStateFlow(false)

    val canUpdate = MutableLiveData(false)

    //TODO add live photos and files

    constructor(point: Point, dbHelper: PointDBHelper, appCtx: Context) : this(dbHelper, appCtx)
    {
        runOnThread(Dispatchers.IO)
        {
            Log.d("RecievedPoint","${point.location.latitude}-${point.location.longitude}")
            val id = point.extraData?.getParameter(2)!!.toLong()
            this.pointRow = dbHelper.getPoint(id) ?: return@runOnThread
            if(!this.pointRow.online || this.pointRow.ownerId == "Me" || this.pointRow.ownerId == "All")
            {
                canUpdate.postValue(true)
            }

            initSymbol(this.pointRow.symbol)
            initLiveData()
            initFiles()
            loading.postValue(false)
        }
    }

    constructor(id: Long, dbHelper: PointDBHelper, appCtx: Context) : this(dbHelper, appCtx)
    {

        runOnThread(Dispatchers.IO)
        {
            this.pointRow = dbHelper.getPoint(id) ?: return@runOnThread
            if(!this.pointRow.online || this.pointRow.ownerId == "Me" || this.pointRow.ownerId == "All")
            {
                canUpdate.postValue(true)
            }

            initSymbol(this.pointRow.symbol)
            initLiveData()
            initFiles()
            loading.postValue(false)
        }
    }

    private fun initLiveData()
    {
        pointName.postValue(pointRow.name)
        pointDescription.postValue(pointRow.descr)
        ownerName.postValue(pointRow.ownerName)
        online.postValue(pointRow.online)
        liveSymbol.postValue(symbol.imageBitmap)

        //TODO
        //ownerState.postValue()
    }

    private fun getThumbnail(uri: Uri)
    {
        val bitmap: Bitmap?
        val mediaMetadataRetriever = MediaMetadataRetriever()

        try {
            mediaMetadataRetriever.setDataSource(appCtx,uri)
            bitmap = mediaMetadataRetriever.getFrameAtTime(0)
            thumbnails.add(Pair(uri,bitmap!!))
        }catch (e: Exception)
        {
            Log.d("getThumbNail","error")
        } finally {
            mediaMetadataRetriever.release()
        }

    }

    private fun getMediaType(uri: Uri): MimeTypes
    {
        val mediaTypeRaw = appCtx.contentResolver.getType(uri)
        return when {
            mediaTypeRaw?.startsWith("image") == true -> MimeTypes.IMAGE
            mediaTypeRaw?.startsWith("video") == true -> MimeTypes.VIDEO
            else -> MimeTypes.UNKNOWN
        }
    }
    private fun initFiles()
    {
        val newList = mutableListOf<PointDetailFileHolder>()
        pointRow.uris?.forEach {
            val flow = service?.pointModel?.checkIfFileIsDownloaded(it)
            val holder: PointDetailFileHolder
            if(flow != null){
                holder = PointDetailFileHolder(it,MutableLiveData(0),getMediaType(it))
                collectFlow(holder,flow)
            } else
            {
                holder = PointDetailFileHolder(it,null,getMediaType(it))
            }
            getThumbnail(it)
            newList.add(holder)
        }
        viewModelScope.launch {
            withContext(Dispatchers.Main){
                uris.value = newList
            }
        }

    }

    private fun collectFlow(holder: PointDetailFileHolder,flow: SharedFlow<DownloadResult>){

        viewModelScope.launch {
            withContext(Dispatchers.IO)
            {
                flow.collect{
                    when(it){
                        is DownloadResult.Progress -> {
                            holder.loadingState?.postValue(it.progress)
                        }
                        is DownloadResult.Success -> {
                            holder.loadingState?.postValue(100)
                            holder.loadingState?.postValue(null)
                        }
                        is DownloadResult.Error -> {
                            holder.loadingState?.postValue(-1)
                        }
                    }
                }
            }
        }

    }

    private fun initSymbol(symbolString: String)
    {
        symbol = Symbol(symbolString,appCtx)
    }

    fun deletePoint(atEnd:  () -> Unit)
    {
        loading.value = true
        runOnThread(Dispatchers.IO)
        {
            dbHelper.removePoint(pointRow.id)
            runOnThread(Dispatchers.Main)
            {
                atEnd()
            }
            ActionDisplayPoints.removePackFromLocus(appCtx, pointRow.id.toString())

            pointRow.uris?.forEach {
                appCtx.contentResolver.delete(it,null,null)
            }
        }
    }
    override fun onOnServiceStateChanged(newState: ConnectionState) {
        if(newState == _connectionState.value) return
        _connectionState.postValue(newState)
    }

    override fun onServiceErroStringChange(new: String) {
        _connectionErrorMsg.postValue(new)
    }
    fun bind()
    {
        if(service != null) return

        val running = appCtx.getSharedPreferences("jmb_bms_Server_Info", Context.MODE_PRIVATE).getBoolean("Service_Running",false)

        if(!running) return
        Log.d("PointCreationVM", "Binding to service")
        val intent = Intent(appCtx, ConnectionService::class.java).putExtra("Caller","CreatePoint")
        appCtx.bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)
    }

    fun unbind()
    {
        service?.unSetCallBack()
        service?.unsetPointCallBacks()

        if( service != null)
        {
            appCtx.unbindService(serviceConnection)
        }
        service = null
    }
    override fun onCleared() {
        super.onCleared()
        unbind()
    }

    companion object{

        fun create(point: Point, dbHelper: PointDBHelper, appCtx: Context): ViewModelProvider.Factory{

            return object : ViewModelProvider.Factory{
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    if( modelClass.isAssignableFrom(PointDetailVM::class.java) )
                    {
                        return PointDetailVM(point, dbHelper, appCtx) as T
                    }
                    throw IllegalArgumentException("Unknown VM class")
                }
            }
        }
        fun create(id: Long, dbHelper: PointDBHelper, appCtx: Context): ViewModelProvider.Factory{

            return object : ViewModelProvider.Factory{
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    if( modelClass.isAssignableFrom(PointDetailVM::class.java) )
                    {
                        return PointDetailVM(id, dbHelper, appCtx) as T
                    }
                    throw IllegalArgumentException("Unknown VM class")
                }
            }
        }

    }

    override fun parsedPoint(id: Long) {

        if(!this::pointRow.isInitialized) return

        if(pointRow.id != id) return

        pointRow = dbHelper.getPoint(id) ?: return

        initLiveData()
        initSymbol(pointRow.symbol)
        initFiles()
    }

    override fun deletedPoint(id: Long) {

        viewModelScope.launch {
            withContext(Dispatchers.Main)
            {
                deleted.value = true
            }
        }
    }
}