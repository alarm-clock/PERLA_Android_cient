/**
 * @file: AllPointsVM.kt
 * @author: Jozef Michal Bukas <xbukas00@stud.fit.vutbr.cz,jozefmbukas@gmail.com>
 * Description: File containing AllPointsVM class
 */
package com.example.jmb_bms.viewModel.point

import android.annotation.SuppressLint
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import android.util.Log
import androidx.compose.runtime.mutableStateListOf
import androidx.core.content.edit
import androidx.lifecycle.*
import com.example.jmb_bms.LocusVersionHolder
import com.example.jmb_bms.connectionService.ConnectionService
import com.example.jmb_bms.connectionService.ConnectionState
import com.example.jmb_bms.connectionService.in_app_communication.PointRelatedCallBacks
import com.example.jmb_bms.connectionService.in_app_communication.ServiceStateCallback
import com.example.jmb_bms.model.LiveServiceState
import com.example.jmb_bms.model.PointMenuRow
import com.example.jmb_bms.model.database.points.PointDBHelper
import com.example.jmb_bms.model.icons.Symbol
import com.example.jmb_bms.model.utils.OperationsOnPoints
import com.example.jmb_bms.model.utils.centerMapInLocusJson
import com.example.jmb_bms.model.utils.runOnThread
import com.example.jmb_bms.viewModel.ServiceBinder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import locus.api.android.ActionDisplayPoints
import locus.api.android.utils.LocusUtils

/**
 * ViewModel for AllPoints screen. It implements all methods required to show and handle point operations on given screen.
 * This class implements interfaces [ServiceStateCallback] and [PointRelatedCallBacks]. Also, it extends [ViewModel] class.
 * @param dbHelper Point database helper
 * @param appCtx Application context for rendering symbols and for binding to service
 * @constructor sets what screen will be shown, prepares points for it and binds to [ConnectionService] if it is running
 */
class AllPointsVM(private val dbHelper: PointDBHelper, @SuppressLint("StaticFieldLeak") val appCtx: Context) : ViewModel(), PointRelatedCallBacks {

    /*
    private var service : ConnectionService? = null

    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, serviceBin: IBinder?) {
            val binder = serviceBin as ConnectionService.LocalBinder
            service = binder.getService()

            Log.d("PointCreationVM","Setting myself as callback to $service")
            service?.setCallBack(this@AllPointsVM)
            service?.setPointCallBacks(this@AllPointsVM)
            //service?.setComplexDataCallBack(this@ServerVM)
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            service = null
        }
    }

     */

    val liveConnectionState = LiveServiceState()
    private val serviceBinder = ServiceBinder(appCtx, listOf(liveConnectionState,this))

    /*
    private val _connectionState = MutableLiveData<ConnectionState>(ConnectionState.NONE)
    val connectionState: LiveData<ConnectionState> = _connectionState

    private val _connectionErrorMsg = MutableLiveData("")
    val connectionErrorMsg: LiveData<String> = _connectionErrorMsg


     */
    private val shPref = appCtx.getSharedPreferences("Point_Menu", Context.MODE_PRIVATE)

    val shownPoints = MutableStateFlow<List<PointMenuRow>?>(null)

    private var pickedListCode = -1

    val pickedListLiveCode = MutableLiveData(-1)

    val loading = MutableLiveData<Boolean>(false)

    val picking = MutableStateFlow(false)

    val pickedPointsIds = mutableStateListOf<Long>()

    var operation = OperationsOnPoints.DELETING
        private set

    init {
        runOnThread(Dispatchers.IO) {
            loading.postValue(true)
           // val running = appCtx.getSharedPreferences("jmb_bms_Server_Info", Context.MODE_PRIVATE).getBoolean("Service_Running",false)
           // if(running) bind()
            pickedListCode = shPref.getInt("Screen",-1) //menu with three options
            selectPoints(pickedListCode,appCtx)
        }
    }

    /**
     * Method that sets which screen is shown based on [type]. If screen shows points those points are fetched from db.
     * This method also sets [loading] to false.
     * @param type Screen code type
     * @param ctx context for rendering symbols
     */
    fun selectPoints(type: Int, ctx: Context)
    {
        pickedListLiveCode.postValue(type)
        if(type == -1)
        {
            loading.postValue(false)
            return
        }

        runOnThread(Dispatchers.IO)
        {
            loading.postValue(true)
            pickedListCode = type
            val pickedList = when(type)
            {
                0 -> dbHelper.getAllPointMenuRow()
                1 -> dbHelper.getUserPointMenuRow(true)
                2 -> dbHelper.getUserPointMenuRow(false)
                else -> null
            }

            val menuRow = pickedList?.map { PointMenuRow(it.id,it.name, Symbol(it.symbol,ctx), it.visible, it.ownedByClient) }

            runOnThread(Dispatchers.Main) {
                shownPoints.value = menuRow
                loading.value = false
            }
        }
    }

    /**
     * Method that resets state of this object and sets category picking screen by setting [pickedListCode] to -1
     */
    fun reset()
    {
        pickedListCode = -1
        shownPoints.value = null

        runOnThread(Dispatchers.IO)
        {
            shPref.edit {
                putInt("Screen",pickedListCode)
                commit()
            }
        }
    }

    /**
     * Method that stores what screen was used when point detail opened
     */
    fun showPointDetail()
    {
        runOnThread(Dispatchers.IO)
        {
            shPref.edit {
                putInt("Screen",pickedListCode)
                commit()
            }
        }
    }

    /**
     * Method that changes point's visibility on map
     * @param id Points id
     * @param value Flag that indicates if point should be shown or removed
     * @param ctx Context for Locus operations
     */
    fun changePointVisibility(id: Long, value: Boolean, ctx: Context)
    {
        runOnThread(Dispatchers.IO)
        {
            dbHelper.updatePointsVisibility(value,id)
            val row = dbHelper.getPoint(id) ?: return@runOnThread

            shownPoints.value?.find { it.id == id  }?.liveVisible?.postValue(value)

            if(value) dbHelper.createAndSendPoint(row,ctx)
            else ActionDisplayPoints.removePackFromLocus(ctx,id.toString())
        }
    }

    /**
     * Method that sets multiple points operation and when called second time deletes all marked points stored in [pickedPointsIds].
     * If [picking] is false this function sets it to true and sets [operation] to [OperationsOnPoints.DELETING].
     * If [picking] is true it deletes all picked points.
     * @param ctx Context for Locus methods and deleting files attached to point
     */
    fun markAndDelete(ctx: Context)
    {
        //deleting points
        if(picking.value)
        {
            runOnThread(Dispatchers.IO)
            {
                loading.postValue(true)
                val tmp = shownPoints.value?.toMutableList()


                pickedPointsIds.forEach {
                    val point = dbHelper.getPoint(it)

                    //if point is online point which user can update send point deletion message to server
                    if(point?.online == true && (point.ownerId == "Me" || point.ownerId == "All") ) serviceBinder.service?.deletePoint(point.serverId)


                    dbHelper.getPoint(it)?.uris?.forEach {uri ->

                        //deleting files through content resolver
                        ctx.contentResolver.delete(uri,null,null)
                    }

                    dbHelper.removePoint(it)
                    tmp?.removeIf { data -> data.id == it }
                    ActionDisplayPoints.removePackFromLocus(ctx,it.toString())


                }
                runOnThread(Dispatchers.Main)
                {
                    shownPoints.value = tmp?.toList()
                    pickedPointsIds.removeIf { true }
                    picking.value = false
                    loading.value = false
                }
            }

        // setting operation and picking value
        } else
        {
            picking.value = true
            operation = OperationsOnPoints.DELETING
        }
    }

    /**
     * Method that calls operation onto multiple points based on the value set in [operation] attribute that is set
     * by given operation.
     * @param ctx Context required for Locus operations and content resolving
     */
    fun finnishMarkingMultiple(ctx: Context)
    {
        when(operation)
        {
            OperationsOnPoints.MAKING_VIS -> markAndMakeVis(ctx)
            OperationsOnPoints.MAKING_INVIS -> markAndMakeInVis(ctx)
            OperationsOnPoints.DELETING -> markAndDelete(ctx)
        }
    }

    /**
     * Method that resets [pickedPointsIds] and [picking] attributes when users decides that he doesn't want to operation
     * with multiple points.
     */
    fun stopPicking()
    {
        pickedPointsIds.removeIf { true }
        picking.value = false
    }

    /**
     * Method that based on the [picking] attribute sets operation or makes multiple points visible. If [picking] is false
     * then it is set to true and [operation] is set to [OperationsOnPoints.MAKING_VIS]. If [picking] is true method
     * puts on map all points identified by ids stored in [pickedPointsIds]
     * @param ctx Context for Locus method
     */
    fun markAndMakeVis(ctx: Context)
    {
        //making points visible
        if(picking.value)
        {
            runOnThread(Dispatchers.IO)
            {
                loading.postValue(true)
                pickedPointsIds.forEach {
                    changePointVisibility(it,true,ctx)
                }
                runOnThread(Dispatchers.Main)
                {
                    pickedPointsIds.removeIf { true }
                    picking.value = false
                    loading.value = false
                }
            }

        //setting operation and picking
        } else
        {
            picking.value = true
            operation = OperationsOnPoints.MAKING_VIS
        }
    }

    /**
     * Method that based on the [picking] attribute sets operation or makes multiple points invisible. If [picking] is false
     * then it is set to true and [operation] is set to [OperationsOnPoints.MAKING_INVIS]. If [picking] is true method
     * removes from map all points identified by ids stored in [pickedPointsIds]
     * @param ctx Context for Locus method
     */
    fun markAndMakeInVis(ctx: Context)
    {
        //removing points from map
        if(picking.value)
        {
            runOnThread(Dispatchers.IO)
            {
                loading.postValue(true)
                pickedPointsIds.forEach {
                    changePointVisibility(it,false,ctx)
                }
                runOnThread(Dispatchers.Main)
                {
                    pickedPointsIds.removeIf { true }
                    picking.value = false
                    loading.value = false
                }
            }
        //setting picking and operation
        } else
        {
            picking.value = true
            operation = OperationsOnPoints.MAKING_INVIS
        }
    }


    /**
     * Method that deletes point based on its [id].
     * @param id ID of point that should be deleted
     * @param ctx Context for Locus method
     */
    fun deletePoint(id: Long,ctx: Context)
    {
        runOnThread(Dispatchers.IO)
        {
            Log.d("ID",id.toString())

            val point = dbHelper.getPoint(id)

            point?.uris?.forEach {
                ctx.contentResolver.delete(it,null,null)
            }
            Log.d("DeletingPoint",point?.online.toString())
            if(point?.online == true && (point.ownerId == "Me" || point.ownerId == "All")) serviceBinder.service?.deletePoint(point.serverId!!)

            dbHelper.removePoint(id)
            val tmp = shownPoints.value?.toMutableList()
            tmp?.removeIf { it.id == id }
            ActionDisplayPoints.removePackFromLocus(ctx,id.toString())

            runOnThread(Dispatchers.Main)
            {
                shownPoints.value = tmp?.toList()
            }
        }
    }

    /**
     * Method that centers map on point identified by [id] and then opens Locus Map.
     * @param id ID of point that will be shown on map
     * @param ctx Context for Locus operation
     */
    fun centerAndOpenLocus(id: Long, ctx: Context)
    {
        val location = dbHelper.getPointsLocation(id) ?: return
        val json = centerMapInLocusJson(location.latitude,location.longitude)
        val intent = Intent("com.asamm.locus.ACTION_TASK").apply {
            setPackage(LocusVersionHolder.getLv()!!.packageName)
            putExtra("tasks",json)
        }
        ctx.sendBroadcast(intent)
        LocusUtils.callStartLocusMap(ctx)
    }

    companion object{

        /**
         * Static method that creates custom vm factory for [AllPointsVM] viewModel with custom parameters.
         * @param context Application context for Locus methods and symbol rendering
         * @param dbHelper DB helper for point operations
         */
        fun create(context: Context, dbHelper: PointDBHelper): ViewModelProvider.Factory{

            return object : ViewModelProvider.Factory{
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    if( modelClass.isAssignableFrom(AllPointsVM::class.java) )
                    {
                        return AllPointsVM(dbHelper, context) as T
                    }
                    throw IllegalArgumentException("Unknown VM class")
                }
            }
        }
    }

    /*
    override fun onOnServiceStateChanged(newState: ConnectionState) {
        if(newState == _connectionState.value) return
        _connectionState.postValue(newState)
    }

    override fun onServiceErroStringChange(new: String) {
        _connectionErrorMsg.postValue(new)
    }

     */

    /*
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

     */
    override fun onCleared() {
        super.onCleared()
        //unbind()

        serviceBinder.unbind()
    }

    override fun parsedPoint(id: Long) {

        //try to take point from db based on picked category. If null is returned that means that point isn't in
        //picked category so nothing happens
        val menuRow = when(pickedListCode)
        {
            0 -> dbHelper.getMenuRowById(id)
            1 -> dbHelper.getMenuRowByIdWithUsers(id,true)
            2 -> dbHelper.getMenuRowByIdWithUsers(id,false)
            else -> null // picking point category screen = do nothing
        } ?: return

        val existingMenuRow = shownPoints.value?.find { it.id == menuRow.id }
        val tmp = shownPoints.value?.toMutableList()

        //if points is already shown that means it is going to be updated so remove it before new version will be added
        if(existingMenuRow != null) tmp?.remove(existingMenuRow)

        tmp?.add(PointMenuRow(menuRow.id,menuRow.name,Symbol(menuRow.symbol,appCtx),true,menuRow.ownedByClient))
        viewModelScope.launch {
            withContext(Dispatchers.Main)
            {
                shownPoints.value = tmp
                Log.d("ParsedPoint","Updated list, list value is: $tmp")
            }
        }
    }

    override fun deletedPoint(id: Long) {

        val tmp = shownPoints.value?.toMutableList() ?: return

        //if point is shown delete it
        tmp.removeIf { it.id == id }

        viewModelScope.launch {
            withContext(Dispatchers.IO){
                shownPoints.value = tmp
            }
        }
    }
}