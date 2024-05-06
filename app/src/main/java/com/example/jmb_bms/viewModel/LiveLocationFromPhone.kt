/**
 * @file: LiveLocationFromPhone.kt
 * @author: Jozef Michal Bukas <xbukas00@stud.fit.vutbr.cz,jozefmbukas@gmail.com>
 * Description: File containing LiveLocationFromPhone class
 */
package com.example.jmb_bms.viewModel

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.HandlerThread
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.lifecycle.*
import com.example.jmb_bms.model.LocationRepo
import com.example.jmb_bms.model.utils.wgs84toMGRS
import com.google.android.gms.location.*
import kotlinx.coroutines.*

/**
 * @param ctx Context used to get FusedLocationProviderClient
 * @param locationRep used when taking location from Locus but changed in last minute so DEPRECATED
 * @constructor Checks all necessary permissions and requests location updates every second with high accuracy.
 * This location is then available in [currLocation] attribute.
 */
class LiveLocationFromPhone(private val locationRep: LocationRepo, ctx: Context) : ViewModel() {

    private val second = 1000L

    private val __location = MutableLiveData<String>()     //this changes over time

    /**
     * Current location in MGRS format
     */
    val currLocation: LiveData<String> get() = __location  // this is getter for current value

    private var collectionJob: Job? = null

    private val ser = LocationServices.getFusedLocationProviderClient(ctx)

    private var cnt = 0

    /**
     * Callback which posts received location into [__location]
     */
    private val locationCallback = object : LocationCallback() {
        /**
         * Callback which is called when new location is received
         * @param p0 LocationResult object with last known location
         */
        override fun onLocationResult(p0: LocationResult) {
            super.onLocationResult(p0)

            val loc = p0.lastLocation ?: return
            __location.postValue(wgs84toMGRS(loc))

            //live data eats lots of memory so every 50 writes it suggest system to call garbage collector
            //garbage collection is usually called after 100 writes which still helps a lot
            if (cnt++ == 50) {
                Log.d("LiveLocationFromPhone", "Suggesting garbage dump")
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


                val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, second).build()
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

        /**
         * Static method for creating custom viewModel factory for LiveLocationFromPhone class
         * @param locationRepo DEPRECATED last minute
         * @param ctx Context used to get FusedLocationProviderClient
         * @return ViewModel factory
         */
        fun create(locationRepo: LocationRepo, ctx: Context): ViewModelProvider.Factory {
            return object : ViewModelProvider.Factory{
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    if (modelClass.isAssignableFrom(LiveLocationFromPhone::class.java)) {
                        return LiveLocationFromPhone(locationRepo, ctx) as T
                    }
                    throw IllegalArgumentException("Unknown ViewModel class")
                }
            }
        }
    }
}