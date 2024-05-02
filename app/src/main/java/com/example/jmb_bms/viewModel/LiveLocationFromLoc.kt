package com.example.jmb_bms.viewModel

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.HandlerThread
import android.os.Looper
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.lifecycle.*
import com.example.jmb_bms.connectionService.ClientMessage
import com.example.jmb_bms.model.LocationRepo
import com.example.jmb_bms.model.utils.wgs84toMGRS
import com.google.android.gms.location.*
import io.ktor.websocket.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collect
import locus.api.android.ActionBasics
import locus.api.android.utils.LocusUtils
import java.util.Timer
import java.util.TimerTask

class LiveLocationFromLoc(private val locationRep: LocationRepo,ctx: Context) : ViewModel() {

    private val __location = MutableLiveData<String>()     //this changes over time
    val currLocation: LiveData<String> get() = __location  // this is getter for current value

    private var collectionJob: Job? = null

    private val ser = LocationServices.getFusedLocationProviderClient(ctx)

    private var cnt = 0

    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(p0: LocationResult) {
            super.onLocationResult(p0)

            val loc = p0.lastLocation ?: return
            __location.postValue(wgs84toMGRS(loc))
            if (cnt++ == 50) {
                Log.d("LiveLocationFromLoc", "Suggesting garbage dump")
                System.gc()
                cnt = 0
            }
        }
    }

    val ioThread = HandlerThread("IOThread").apply {
        start()
    }

    init{
        collectionJob = viewModelScope.launch {
            withContext(Dispatchers.IO)
            {
                /*
                locationRep.getLocUpdates().collect{ update -> //collecting from locationRep flow
                    __location.postValue( update )
                    Log.d("LocUpdate",update)

                }

                 */

                if (ActivityCompat.checkSelfPermission(
                        ctx,
                        Manifest.permission.ACCESS_FINE_LOCATION
                    ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                        ctx,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    return@withContext
                }


                val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 1000).build()
                ser.requestLocationUpdates(locationRequest,locationCallback, ioThread.looper)
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        ser.removeLocationUpdates(locationCallback)
        collectionJob?.cancel()
        ioThread.quitSafely()
    }

    companion object {
        //static function for creating factory with additional parameter locationRepo
        //it is used as it would be used if factory was separate object
        //it is better to have it together
        fun create(locationRepo: LocationRepo, ctx: Context): ViewModelProvider.Factory {
            return object : ViewModelProvider.Factory{
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    if (modelClass.isAssignableFrom(LiveLocationFromLoc::class.java)) {
                        return LiveLocationFromLoc(locationRepo, ctx) as T
                    }
                    throw IllegalArgumentException("Unknown ViewModel class")
                }
            }
        }
    }
}