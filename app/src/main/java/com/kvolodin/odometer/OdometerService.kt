package com.kvolodin.odometer

import android.Manifest
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Criteria
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Binder
import android.os.Bundle
import android.os.IBinder
import androidx.core.app.ActivityCompat


enum class CurrentState(val state : Int) {
    STATE_STOP(0),
    STATE_START(1),
    STATE_RESTART(2);

    fun isDuring(): Boolean {
        return this == STATE_START
    }
    fun isRestart():Boolean {
        return this == STATE_RESTART
    }
}


class OdometerService : Service() {
    companion object {
        const val PERMISSION_STRING = Manifest.permission.ACCESS_FINE_LOCATION
    }

    private lateinit var listener: LocationListener
    private lateinit var  locManager : LocationManager

    var currentState : CurrentState = CurrentState.STATE_STOP

    private var distanceInMeters : Double = 0.0
    private var lastLocation : Location? = null

    private val binder = OdometerBinder()
    inner class OdometerBinder : Binder() {
        fun getOdometer() : OdometerService? {
            return this@OdometerService
        }
    }
    override fun onBind(intent: Intent): IBinder {
        return binder
    }

    override fun onCreate() {
        super.onCreate()

        locManager =
            getSystemService(Context.LOCATION_SERVICE) as LocationManager

        val provider = locManager.getBestProvider(Criteria(), true)

        listener = object : LocationListener {
            override fun onLocationChanged(location: Location?) {

                if(currentState.isRestart()) {
                    distanceInMeters = 0.0
                    currentState = CurrentState.STATE_START
                }

                if ( lastLocation == null ) {
                    lastLocation = location
                }
                if( currentState.isDuring() && location != null ){
                    distanceInMeters += location?.distanceTo(lastLocation)
                }
                lastLocation = location;
            }

            override fun onProviderDisabled(arg0: String) {}
            override fun onProviderEnabled(arg0: String) {}
            override fun onStatusChanged(arg0: String, arg1: Int, bundle: Bundle) {}
        }

        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        else {
            locManager.requestLocationUpdates(provider, 100, 0.1f, listener)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        else{
            locManager.removeUpdates(listener)
        }
    }

    fun getDistance() = distanceInMeters
}