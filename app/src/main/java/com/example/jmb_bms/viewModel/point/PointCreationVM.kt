/**
 * @file: PointCreationVM.kt
 * @author: Jozef Michal Bukas <xbukas00@stud.fit.vutbr.cz,jozefmbukas@gmail.com>
 * Description: File containing PointCreationVM class
 */
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
import com.example.jmb_bms.model.LiveServiceState
import com.example.jmb_bms.model.ServerEditingIconModel
import com.example.jmb_bms.model.icons.SymbolCreationVMHelper
import com.example.jmb_bms.model.database.points.PointDBHelper
import com.example.jmb_bms.model.database.points.PointRow
import com.example.jmb_bms.model.icons.Icon
import com.example.jmb_bms.model.icons.Symbol
import com.example.jmb_bms.model.utils.MimeTypes
import com.example.jmb_bms.model.utils.PhotoContract
import com.example.jmb_bms.model.utils.VideoContract
import com.example.jmb_bms.model.utils.runOnThread
import com.example.jmb_bms.viewModel.ServiceBinder
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

/**
 * VM that holds all data and methods required for creating and updating point.
 * @param appCtx Application context for Locus methods, symbol rendering and starting other activities
 * @param activityResultRegistry Result registry for registering callbacks that parses results from other activities like camera
 * @param dbHelper DB helper for points database
 * @constructor Registers result callbacks using [activityResultRegistry] and sets default symbol to [symbolCreationVMHelper]
 */
class PointCreationVM(
    @SuppressLint("StaticFieldLeak") val appCtx: Context,
    private val activityResultRegistry: ActivityResultRegistry,
    private val dbHelper: PointDBHelper,
    creating: Boolean
): ViewModel() {

    val liveServiceState = LiveServiceState()
    private val serviceBinder = ServiceBinder(appCtx, listOf(liveServiceState))

    /**
     * Secondary constructor that takes points [id] and finds it database. After that initializes itself with values from
     * obtained pointRow. This constructor is used for point updating.
     * @param id ID of point that will be updated
     * @param appCtx Application context for Locus methods, symbol rendering and starting other activities
     * @param activityResultRegistry Result registry for registering callbacks that parses results from other activities like camera
     * @param dbHelper DB helper for points database
     */
    constructor(id: Long, appCtx: Context, activityResultRegistry: ActivityResultRegistry, dbHelper: PointDBHelper) : this(appCtx, activityResultRegistry, dbHelper, false)
    {
        runOnThread(Dispatchers.IO)
        {
            loading.postValue(true)
            updating.postValue(true)
            val row = dbHelper.getPoint(id) ?: return@runOnThread

            initFromRow(row)

           // bind()
            loading.postValue(false)
        }
    }

    /**
     * Secondary constructor used when points location is updated and that Location is received from Locus. Firstly it initializes
     * from spacial table in database (stores own state in there) and then updates points location from [locBundle].
     * @param locBundle Bundle with new location
     * @param appCtx Application context for Locus methods, symbol rendering and starting other activities
     * @param activityResultRegistry Result registry for registering callbacks that parses results from other activities like camera
     * @param dbHelper DB helper for points database
     */
    constructor(locBundle: Bundle?, appCtx: Context, activityResultRegistry: ActivityResultRegistry, dbHelper: PointDBHelper) : this(appCtx, activityResultRegistry, dbHelper, false)
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

    /**
     * Method that initializes all important attributes with data from [row]
     * @param row [PointRow] DB entry of point which is updated
     */
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

        setSymbol(row.symbol, row.menuString)

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

    private lateinit var takePhotoLauncher : ActivityResultLauncher<Unit>
    private lateinit var takeVideoLauncher : ActivityResultLauncher<Unit>


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
         //   val running = appCtx.getSharedPreferences("jmb_bms_Server_Info", Context.MODE_PRIVATE).getBoolean("Service_Running",false)
         //   if(running) bind()

            takePhotoLauncher = activityResultRegistry.register("takePhoto", PhotoContract()){
                storeNewFileUri(it)
            }

            takeVideoLauncher = activityResultRegistry.register("takeVideo", VideoContract()){
                storeNewFileUri(it)
            }

            if(creating){
                setSymbol("SPGP-----------","G|-")
            }
            loading.postValue(false)
        }
    }

    /**
     * Method that prepares [symbolCreationVMHelper]
     * @param symbolString String with symbol code
     * @param menuString Formatted string from which menu is constructed
     */
    private fun setSymbol(symbolString: String, menuString: String)
    {
        symbolCreationVMHelper.model.menuIdsString = menuString
        symbolCreationVMHelper.prepareMenuFromTheString(symbolString,appCtx)
        symbolCreationVMHelper.model.symbol = Symbol(symbolString,appCtx,"450")
        symbolCreationVMHelper.bitMap.postValue(symbolCreationVMHelper.model.symbol.imageBitmap)
    }

    /**
     *  Method that gets file's media type from its [Uri]
     *  @param uri [Uri] of file whose type we want
     *  @return [MimeTypes] value representing file type or [MimeTypes.UNKNOWN] if file type is unknown or unsupported
     */
    private fun getMediaType(uri: Uri): MimeTypes
    {
        val mediaTypeRaw = appCtx.contentResolver.getType(uri)
        return when {
            mediaTypeRaw?.startsWith("image") == true -> MimeTypes.IMAGE
            mediaTypeRaw?.startsWith("video") == true -> MimeTypes.VIDEO
            else -> MimeTypes.UNKNOWN
        }
    }

    /**
     * Method that stores file [Uri] in point
     * @param uri File [Uri]
     */
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
                liveServiceState.onServiceErroStringChange("Could not get document URI")
            }

        }
    }

    /**
     * Method that edits points name
     * @param newName
     */
    fun editName(newName: String)
    {
        pointName.value = newName
        runOnThread(Dispatchers.IO)
        {
            _pointName = newName
        }
    }

    /**
     * Method that edits points description
     * @param newDescr
     */
    fun editDescription(newDescr: String)
    {
        pointDescription.value = newDescr
        runOnThread(Dispatchers.IO)
        {
            _pointDescr = newDescr
        }
    }

    /**
     * Method that stores vm's current state and launches [GetLocFromLocActivity]
     * @param context Context for starting activity
     */
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

    /**
     * Method that launches take photo activity from [takePhotoLauncher] if device has camera.
     */
    fun takePhotoFromCamera()
    {
        val hasCamera = appCtx.packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY)
        if(hasCamera)
        {
            takePhotoLauncher.launch(Unit)
        } else
        {
            liveServiceState.onServiceErroStringChange("This device has no camera")
        }
    }

    /**
     * Method that launches take video activity from [takeVideoLauncher] if device has camera.
     * UNUSED due to bug that prevents upload of longer videos. When bug will be fixed just uncomment it in view
     */
    fun takeVideoFromCamera()
    {
        val hasCamera = appCtx.packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY)
        if(hasCamera)
        {
            takeVideoLauncher.launch(Unit)
        } else
        {
            liveServiceState.onServiceErroStringChange("This device has no camera")
        }
    }

    /**
     * Method that deletes file on given [path]
     * @param path Path to file
     */
    fun removeUri(path: String)
    {
        runOnThread(Dispatchers.IO)
        {
            val tmp = liveUris.value.toMutableList()
            tmp.removeIf{ it.first == path}
            addedDocuments.removeIf { it.toString() == path }

            //if point is updated and user decides to leave without updating, so I don't delete files right away
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

    /**
     * Method that sends point to Locus Map adds it ID and sets [SetAllPointsOnMapActivity] as callback when point is
     * touched. After that it renders points symbol and then sends point to Locus Map.
     * @param newPoint Point that will be sent to Locus Map
     * @param id Points ID in clients database
     */
    private fun sendPointToLocus(newPoint: Point,id: Long)
    {
        Log.d("PointCreationVM", id.toString())
        newPoint.extraData!!.addParameter(2,id.toString())
        newPoint.setExtraOnDisplay(
            "com.example.jmb_bms",
            "com.example.jmb_bms.activities.SetAllPointsOnMapActivity",
            "op", //not used so random values
            "e"
        )

        val packPoints = PackPoints(id.toString())
        packPoints.bitmap = symbolCreationVMHelper.model.symbol.imageBitmap!!.asAndroidBitmap()
        packPoints.addPoint(newPoint)
        ActionDisplayPoints.sendPackSilent(appCtx,packPoints,true)
    }

    /**
     * Method that creates point offline
     */
    fun createPointOffline()
    {
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

    /**
     * Method that creates point and sends it to server
     */
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
            serviceBinder.service?.sendPoint(id)
        }
    }

    /**
     * Method that creates [PointRow] and stores it in database.
     * @param newPoint Point object that holds points location
     * @param online Flag that indicates if points is created online or offline
     * @return Points ID in local database
     */
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

    /**
     * Method that creates point that can be sent to Locus. It initializes all necessary data like name or adds tag
     * by which client resolves which point is his and which is from Locus.
     * @return Point with all data stored in it with DB ID only missing
     */
    private fun createPoint(): Point
    {
        val newPoint = Point("",point.location)

        //if no value is entered it gives name of symbol to point
        if( _pointName == "" )
        {
            _pointName = symbolCreationVMHelper.selectedOptStack.value.last().value.iconTuple!!.iconName
        }
        //checkIfPointHasUniqueName()

        newPoint.name = _pointName
        newPoint.extraData = GeoDataExtra()
        newPoint.extraData!!.addParameter(1,"jmb_bms") //tag by which client knows origin of point
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

    /**
     * Method that sets existing point stored in [point] attribute with new values.
     */
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
                "com.example.jmb_bms.activities.SetAllPointsOnMapActivity",
                "op",
                "e"
            )

        addedDocuments.forEach{
            val res =  point.addAttachmentPhoto(it.toString(),"photo")
            Log.d("RESULT", res.toString())
        }
    }

    /**
     * Method that updates point and its database entry and if point is online point it will also send point update message
     * @param atEnd Closure that is invoked on main thread after all operations happen. It is here because of whole
     * simulating backward movement thing caused by getting location from Locus.
     */
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

            serviceBinder.service?.pointModel?.sendUpdatePoint(row.id)

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

    companion object{

        /**
         * Static method that creates custom vm factory for [PointCreationVM] that invokes primary constructor.
         * @param context Application context for Locus methods, symbol rendering and starting other activities
         * @param activityResultRegistry Result registry for registering callbacks that parses results from other activities like camera
         * @param dbHelper DB helper for points database
         */
        fun create(context: Context, activityResultRegistry: ActivityResultRegistry,dbHelper: PointDBHelper): ViewModelProvider.Factory{

            return object : ViewModelProvider.Factory{
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    if( modelClass.isAssignableFrom(PointCreationVM::class.java) )
                    {
                        return PointCreationVM(context, activityResultRegistry, dbHelper, true) as T
                    }
                    throw IllegalArgumentException("Unknown VM class")
                }
            }
        }

        /**
         * Static method that creates custom vm factory for [PointCreationVM] that invokes secondary constructor that
         * initializes [PointCreationVM] from point identified by [id] and sets [updating] to true.
         * @param id ID of point that will be updated
         * @param context Application context for Locus methods, symbol rendering and starting other activities
         * @param activityResultRegistry Result registry for registering callbacks that parses results from other activities like camera
         * @param dbHelper DB helper for points database
         */
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

        /**
         * Static method that creates custom vm factory for [PointCreationVM] that invokes secondary constructor that initializes
         * [PointCreationVM] with state stored in database and updates points location with location stored in [locBundle].
         * @param locBundle Bundle with new location
         * @param context Application context for Locus methods, symbol rendering and starting other activities
         * @param activityResultRegistry Result registry for registering callbacks that parses results from other activities like camera
         * @param dbHelper DB helper for points database
         */
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
        serviceBinder.unbind()
    }
}