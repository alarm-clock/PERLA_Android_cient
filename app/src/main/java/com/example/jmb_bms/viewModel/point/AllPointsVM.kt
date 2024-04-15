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
import com.example.jmb_bms.model.PointMenuRow
import com.example.jmb_bms.model.database.points.PointDBHelper
import com.example.jmb_bms.model.icons.Symbol
import com.example.jmb_bms.model.utils.OperationsOnPoints
import com.example.jmb_bms.model.utils.centerMapInLocusJson
import com.example.jmb_bms.model.utils.runOnThread
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import locus.api.android.ActionDisplayPoints
import locus.api.android.utils.LocusUtils

class AllPointsVM(private val dbHelper: PointDBHelper, @SuppressLint("StaticFieldLeak") val appCtx: Context) : ViewModel(), ServiceStateCallback, PointRelatedCallBacks {

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

    private val _connectionState = MutableLiveData<ConnectionState>(ConnectionState.NONE)
    val connectionState: LiveData<ConnectionState> = _connectionState

    private val _connectionErrorMsg = MutableLiveData("")
    val connectionErrorMsg: LiveData<String> = _connectionErrorMsg

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
            val running = appCtx.getSharedPreferences("jmb_bms_Server_Info", Context.MODE_PRIVATE).getBoolean("Service_Running",false)
            if(running) bind()
            pickedListCode = shPref.getInt("Screen",-1)
            selectPoints(pickedListCode,appCtx)
        }
    }

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

    fun markAndDelete(ctx: Context)
    {
        if(picking.value)
        {
            runOnThread(Dispatchers.IO)
            {
                loading.postValue(true)
                val tmp = shownPoints.value?.toMutableList()


                pickedPointsIds.forEach {
                    val point = dbHelper.getPoint(it)
                    if(point?.online == true && (point.ownerId == "Me" || point.ownerId == "All") ) service?.deletePoint(point.serverId!!)
                    dbHelper.getPoint(it)?.uris?.forEach {uri ->
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

        } else
        {
            picking.value = true
            operation = OperationsOnPoints.DELETING
        }
    }

    fun finnishMarkingMultiple(ctx: Context)
    {
        when(operation)
        {
            OperationsOnPoints.MAKING_VIS -> markAndMakeVis(ctx)
            OperationsOnPoints.MAKING_INVIS -> markAndMakeInVis(ctx)
            OperationsOnPoints.DELETING -> markAndDelete(ctx)
        }
    }

    fun stopPicking()
    {
        pickedPointsIds.removeIf { true }
        picking.value = false
    }

    fun markAndMakeVis(ctx: Context)
    {
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

        } else
        {
            picking.value = true
            operation = OperationsOnPoints.MAKING_VIS
        }
    }

    fun markAndMakeInVis(ctx: Context)
    {
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

        } else
        {
            picking.value = true
            operation = OperationsOnPoints.MAKING_INVIS
        }
    }


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
            if(point?.online == true && (point.ownerId == "Me" || point.ownerId == "All")) service?.deletePoint(point.serverId!!)

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

    override fun parsedPoint(id: Long) {

        Log.d("ParsedPoint","Got in here, pickedListIs: $pickedListCode")
        val menuRow = when(pickedListCode)
        {
            0 -> dbHelper.getMenuRowById(id)
            1 -> dbHelper.getMenuRowByIdWithUsers(id,true)
            2 -> dbHelper.getMenuRowByIdWithUsers(id,false)
            else -> null
        } ?: return

        val existingMenuRow = shownPoints.value?.find { it.id == menuRow.id }

        Log.d("ParsedPoint","existing menu row is: $existingMenuRow")

        val tmp = shownPoints.value?.toMutableList()

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

        tmp.removeIf { it.id == id }

        viewModelScope.launch {
            withContext(Dispatchers.IO){
                shownPoints.value = tmp
            }
        }
    }
}