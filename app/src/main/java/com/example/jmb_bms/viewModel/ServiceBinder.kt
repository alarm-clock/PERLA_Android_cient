package com.example.jmb_bms.viewModel

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.jmb_bms.connectionService.ConnectionService
import com.example.jmb_bms.connectionService.ConnectionState
import com.example.jmb_bms.connectionService.in_app_communication.*

class ServiceBinder(private val appCtx: Context, private val callBacksForRegister: List<Any>){

    var service : ConnectionService? = null

    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, serviceBin: IBinder?) {
            val binder = serviceBin as ConnectionService.LocalBinder
            service = binder.getService()
            setCallbacks()
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            service = null
        }
    }
    init {
        bind()
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
        unsetCallBacks()
        if( service != null)
        {
            appCtx.unbindService(serviceConnection)
        }
        service = null
    }

    private fun setCallbacks()
    {
        callBacksForRegister.forEach {
            when(it){
                is ComplexServiceStateCallBacks -> service?.setComplexDataCallBack(it)
                is LiveUsersCallback -> service?.setLiveUsersCallBack(it)
                is PointRelatedCallBacks -> service?.setPointCallBacks(it)
                is ServiceStateCallback -> service?.setCallBack(it)
                is ChatRoomsCallBacks -> service?.setChatRoomsCallBack(it)
            }
        }
    }

    private fun unsetCallBacks()
    {
        callBacksForRegister.forEach {
            when(it){
                is ComplexServiceStateCallBacks -> service?.unSetComplexDataCallBack()
                is LiveUsersCallback -> service?.unsetLiveUsersCallBack()
                is PointRelatedCallBacks -> service?.unsetPointCallBacks()
                is ServiceStateCallback -> service?.unSetCallBack()
                is ChatRoomsCallBacks -> service?.unsetChatRoomsCallBack()
            }
        }
    }

}