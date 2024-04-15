package com.example.jmb_bms.viewModel.point

import android.annotation.SuppressLint
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.ActivityResultRegistry
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.lifecycle.*
import com.example.jmb_bms.LocusVersionHolder
import com.example.jmb_bms.activities.GetLocFromLocActivity
import com.example.jmb_bms.connectionService.ConnectionService
import com.example.jmb_bms.connectionService.ConnectionState
import com.example.jmb_bms.connectionService.in_app_communication.ServiceStateCallback
import com.example.jmb_bms.model.ServerEditingIconModel
import com.example.jmb_bms.model.icons.SymbolCreationVMHelper
import com.example.jmb_bms.model.database.points.PointDBHelper
import com.example.jmb_bms.model.database.points.PointRow
import com.example.jmb_bms.model.icons.Symbol
import com.example.jmb_bms.model.utils.MimeTypes
import com.example.jmb_bms.model.utils.PhotoContract
import com.example.jmb_bms.model.utils.VideoContract
import com.example.jmb_bms.model.utils.runOnThread
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import locus.api.android.ActionBasics
import locus.api.android.ActionDisplayPoints
import locus.api.android.objects.PackPoints
import locus.api.objects.extra.GeoDataExtra
import locus.api.objects.geoData.Point
import locus.api.objects.geoData.addAttachmentPhoto
import locus.api.objects.geoData.parameterDescription
import java.util.concurrent.CopyOnWriteArrayList

class PointCreationVM(
    @SuppressLint("StaticFieldLeak") val appCtx: Context,
    private val activityResultRegistry: ActivityResultRegistry,
    private val dbHelper: PointDBHelper

): ViewModel(), ServiceStateCallback {

    constructor(id: Long, appCtx: Context, activityResultRegistry: ActivityResultRegistry, dbHelper: PointDBHelper) : this(appCtx, activityResultRegistry, dbHelper)
    {
        runOnThread(Dispatchers.IO)
        {
            loading.postValue(true)
            updating.postValue(true)
            val row = dbHelper.getPoint(id) ?: return@runOnThread

            initFromRow(row)

            bind()
            loading.postValue(false)
        }
    }

    constructor(locBundle: Bundle?, appCtx: Context, activityResultRegistry: ActivityResultRegistry, dbHelper: PointDBHelper) : this(appCtx, activityResultRegistry, dbHelper)
    {
        runOnThread(Dispatchers.IO)
        {
            loading.postValue(true)
            updating.postValue(true)
            val row = dbHelper.getTmpRow()

            locBundle?.let{
                Log.d("Location","${row.location.latitude}-${row.location.longitude}")
                row.location.latitude = it.getDouble("lat")
                row.location.longitude = it.getDouble("long")
                Log.d("Location","${row.location.latitude}-${row.location.longitude}")
            }

            initFromRow(row)
            loading.postValue(false)
        }
    }

    private fun initFromRow(row: PointRow)
    {
        this.row = row

        point = Point(row.name,row.location)
        //point.location = row.location
        _pointName = row.name
        _pointDescr = row.descr

        row.uris?.forEach {
            addedDocuments.add(it)
        }

        val pairs = this.row.uris?.map { Pair(it.toString(), getMediaType(it)) }

        symbolCreationVMHelper.model.menuIdsString = row.menuString
        symbolCreationVMHelper.prepareMenuFromTheString(row.symbol,appCtx)
        symbolCreationVMHelper.model.symbol = Symbol(row.symbol,appCtx,"450")
        symbolCreationVMHelper.bitMap.postValue(symbolCreationVMHelper.model.symbol.imageBitmap)

        runOnThread(Dispatchers.Main)
        {
            pointName.value = this.row.name
            pointDescription.value = this.row.descr
            liveUris.value = pairs ?: listOf()
        }
    }

    lateinit var row : PointRow

    val updating = MutableLiveData(false)
    val deletedFiles = mutableListOf<Uri>()

    private val _connectionState = MutableLiveData<ConnectionState>(ConnectionState.NONE)
    val connectionState: LiveData<ConnectionState> = _connectionState

    private val _connectionErrorMsg = MutableLiveData("")
    val connectionErrorMsg: LiveData<String> = _connectionErrorMsg


    private lateinit var takePhotoLauncher : ActivityResultLauncher<Unit>
    private lateinit var takeVideoLauncher : ActivityResultLauncher<Unit>

    private var service : ConnectionService? = null

    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, serviceBin: IBinder?) {
            val binder = serviceBin as ConnectionService.LocalBinder
            service = binder.getService()

            Log.d("PointCreationVM","Setting myself as callback to $service")
            service?.setCallBack(this@PointCreationVM)
            //service?.setComplexDataCallBack(this@ServerVM)
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            service = null
        }
    }

    lateinit var point: Point
    val pointName = MutableLiveData("")
    private var _pointName = ""
    val pointDescription = MutableLiveData("")
    private var _pointDescr = ""
    val online = MutableLiveData(true)
    private var _online = true
    val loading = MutableLiveData(true)

    val owner = MutableStateFlow("Me")
    val ownerList = listOf("All","Me")        //add teams and specific users

    val addedDocuments = CopyOnWriteArrayList<Uri>()

    val liveUris = MutableStateFlow(listOf<Pair<String,MimeTypes>>())
    //TODO learn how to show videos or documents in ui


    val everyThingEntered = MutableLiveData(false)

    val symbolCreationVMHelper = SymbolCreationVMHelper(ServerEditingIconModel(appCtx),everyThingEntered,true){ context, runnable ->
        runOnThread(context,runnable)
    }

    var bitmap: Bitmap? = null
    init {

        runOnThread(Dispatchers.IO)
        {
            val running = appCtx.getSharedPreferences("jmb_bms_Server_Info", Context.MODE_PRIVATE).getBoolean("Service_Running",false)
            if(running) bind()

            takePhotoLauncher = activityResultRegistry.register("takePhoto", PhotoContract()){
                storeNewFileUri(it)
            }

            takeVideoLauncher = activityResultRegistry.register("takeVideo", VideoContract()){
                storeNewFileUri(it)
            }
            loading.postValue(false)
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

    private fun storeNewFileUri(uri: Uri?)
    {
        if(uri != null)
        {
            addedDocuments.add(uri)

            val tmp = liveUris.value.toMutableList()

            tmp.add(0, Pair(uri.toString(),getMediaType(uri)))

            runOnThread(Dispatchers.Main)
            {
                liveUris.value = tmp
            }

        } else
        {
            runOnThread(Dispatchers.Main)
            {
                Log.d("PointCreationVM","Could not obtain uri for some reason")
                _connectionErrorMsg.postValue("Could not get document URI")
            }

        }
    }

    fun editName(newName: String)
    {
        pointName.value = newName
        runOnThread(Dispatchers.IO)
        {
            _pointName = newName
        }
    }

    fun editDescription(newDescr: String)
    {
        pointDescription.value = newDescr
        runOnThread(Dispatchers.IO)
        {
            _pointDescr = newDescr
        }
    }
    fun editLocation(context: Context)
    {
        runOnThread(Dispatchers.IO)
        {
            Log.d("EditLocation","Id is: ${row.id.toString()}")
            row.name = _pointName
            row.descr = _pointDescr
            row.uris = addedDocuments
            row.symbol = symbolCreationVMHelper.model.symbol.getSymbolCode()
            dbHelper.storeTmpRow(row)
            val intent = Intent(context,GetLocFromLocActivity::class.java).apply {
                putExtra("caller","point")
            }
            context.startActivity(intent)
        }
    }

    fun takePhotoFromCamera()
    {
        val hasCamera = appCtx.packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY)
        if(hasCamera)
        {
            takePhotoLauncher.launch(Unit)
        } else
        {
            _connectionErrorMsg.postValue("This device has no camera")
        }
    }

    fun takeVideoFromCamera()
    {
        val hasCamera = appCtx.packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY)
        if(hasCamera)
        {
            takeVideoLauncher.launch(Unit)
        } else
        {
            _connectionErrorMsg.postValue("This device has no camera")
        }
    }

    fun removeUri(path: String)
    {
        runOnThread(Dispatchers.IO)
        {
            val tmp = liveUris.value.toMutableList()
            tmp.removeIf{ it.first == path}
            addedDocuments.removeIf { it.toString() == path }

            if(updating.value == true)
            {
                deletedFiles.add(Uri.parse(path))
            } else
            {
                appCtx.contentResolver.delete(Uri.parse(path),null,null)
            }
            runOnThread(Dispatchers.Main)
            {
                liveUris.value = tmp
            }
        }

    }

    private fun sendPointToLocus(newPoint: Point,id: Long)
    {
        Log.d("PointCreationVM", id.toString())
        newPoint.extraData!!.addParameter(2,id.toString())
        newPoint.setExtraOnDisplay(
            "com.example.jmb_bms",
            "com.example.jmb_bms.activities.DummyActivity",
            "op",
            "e"
        )

        val packPoints = PackPoints(id.toString())
        packPoints.bitmap = symbolCreationVMHelper.model.symbol.imageBitmap!!.asAndroidBitmap()
        packPoints.addPoint(newPoint)
        ActionDisplayPoints.sendPackSilent(appCtx,packPoints,true)
    }

    fun createPointOffline()
    {
        //TODO check if everything is in place

        runOnThread(Dispatchers.IO)
        {
            val newPoint = createPoint()

            val id = storePointInDb(newPoint,false)

            if(id == -1L)
            {
                Log.d("PointCreationVM", "Could not insert point")
                return@runOnThread
            }
            sendPointToLocus(newPoint, id)
        }
    }

    fun createPointOnline()
    {
        runOnThread(Dispatchers.IO)
        {
            val newPoint = createPoint()
            val id = storePointInDb(newPoint,true)
            if(id == -1L)
            {
                Log.d("PointCreationVM", "Could not insert point")
                return@runOnThread
            }
            sendPointToLocus(newPoint, id)
            service?.sendPoint(id)
        }
    }

    private fun storePointInDb(newPoint: Point,online: Boolean): Long
    {
        val pointRow = PointRow(
            69 /*nice*/,
            _pointName,
            online,
            if(online) owner.value else "Me",null,null,false,
            newPoint.location,
            symbolCreationVMHelper.model.symbolString,
            _pointDescr,
            true,
            symbolCreationVMHelper.model.menuIdsString,
            addedDocuments.toMutableList()
        )
        return dbHelper.addPoint(pointRow)
    }
    private fun createPoint(): Point
    {
        val newPoint = Point("",point.location)

        if( _pointName == "" )
        {
            _pointName = symbolCreationVMHelper.selectedOptStack.value.last().value.iconTuple!!.iconName
        }
        //checkIfPointHasUniqueName()

        newPoint.name = _pointName
        newPoint.extraData = GeoDataExtra()
        newPoint.extraData!!.addParameter(1,"jmb_bms")
        newPoint.parameterDescription = _pointDescr

        addedDocuments.forEach{
            val res =  newPoint.addAttachmentPhoto(it.toString(),"photo")
            Log.d("RESULT", res.toString())
        }
        point.isEnabled = false
        point.isSelected = false
        point.isVisible = false
        ActionBasics.updatePoint(appCtx,LocusVersionHolder.getLvWithStore(appCtx)!!,point,true)
        newPoint.protected = false
        return newPoint
    }

    private fun createPointForUpdate()
    {
        Log.d("RecievedPoint","${point.location.latitude}-${point.location.longitude}")
        point.name = _pointName
        point.extraData = GeoDataExtra()
        point.extraData!!.addParameter(1,"jmb_bms")
        point.parameterDescription = _pointDescr
        point.protected = false
        point.isSelected = false
        point.extraData!!.addParameter(2,row.id.toString())
        point.setExtraOnDisplay(
                "com.example.jmb_bms",
                "com.example.jmb_bms.activities.DummyActivity",
                "op",
                "e"
            )

        addedDocuments.forEach{
            val res =  point.addAttachmentPhoto(it.toString(),"photo")
            Log.d("RESULT", res.toString())
        }
    }


    fun updatePoint(atEnd:  () -> Unit)
    {
        runOnThread(Dispatchers.IO)
        {
            loading.postValue(true)

            if(_pointName == "")
            {
                _pointName = symbolCreationVMHelper.selectedOptStack.value.last().value.text
            }

            row.name = _pointName
            row.descr = _pointDescr
            row.symbol = symbolCreationVMHelper.model.symbol.getSymbolCode()
            row.uris = addedDocuments.toMutableList()
            row.menuString = symbolCreationVMHelper.model.menuIdsString
            row.ownerId = if(row.online) owner.value else "Me"

            deletedFiles.forEach {
                appCtx.contentResolver.delete(it,null,null)
            }

            dbHelper.updatePointIdentById(row)
            createPointForUpdate()

            service?.pointModel?.sendUpdatePoint(row.id)

            Log.d("PointCreationVM", row.id.toString())
            ActionDisplayPoints.removePackFromLocus(appCtx,row.id.toString())

            val packPoints = PackPoints(row.id.toString())
            packPoints.bitmap = symbolCreationVMHelper.model.symbol.imageBitmap!!.asAndroidBitmap()
            packPoints.addPoint(point)

            ActionDisplayPoints.sendPackSilent(appCtx,packPoints,false)

            runOnThread(Dispatchers.Main)
            {
                atEnd()
            }

        }
    }

    override fun onOnServiceStateChanged(newState: ConnectionState) {
        if(newState == _connectionState.value) return
        Log.d("PointCreationVM","New State: $newState")
        _connectionState.postValue(newState)

        //TODO add/remove online/offline option based on the state change
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

        if( service != null)
        {
            appCtx.unbindService(serviceConnection)
        }
        service = null
    }
    companion object{

        fun create(context: Context, activityResultRegistry: ActivityResultRegistry,dbHelper: PointDBHelper): ViewModelProvider.Factory{

            return object : ViewModelProvider.Factory{
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    if( modelClass.isAssignableFrom(PointCreationVM::class.java) )
                    {
                        return PointCreationVM(context, activityResultRegistry, dbHelper) as T
                    }
                    throw IllegalArgumentException("Unknown VM class")
                }
            }
        }
        fun create(id: Long,context: Context, activityResultRegistry: ActivityResultRegistry,dbHelper: PointDBHelper): ViewModelProvider.Factory{

            return object : ViewModelProvider.Factory{
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    if( modelClass.isAssignableFrom(PointCreationVM::class.java) )
                    {
                        return PointCreationVM(id,context, activityResultRegistry, dbHelper) as T
                    }
                    throw IllegalArgumentException("Unknown VM class")
                }
            }
        }
        fun create(locBundle: Bundle? ,context: Context, activityResultRegistry: ActivityResultRegistry, dbHelper: PointDBHelper): ViewModelProvider.Factory{

            return object : ViewModelProvider.Factory{
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    if( modelClass.isAssignableFrom(PointCreationVM::class.java) )
                    {
                        return PointCreationVM(locBundle,context, activityResultRegistry, dbHelper) as T
                    }
                    throw IllegalArgumentException("Unknown VM class")
                }
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        unbind()
    }
}