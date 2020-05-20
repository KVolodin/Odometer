package com.kvolodin.odometer.modelView

import android.content.ComponentName
import android.content.ServiceConnection
import android.os.IBinder
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.kvolodin.odometer.CurrentState
import com.kvolodin.odometer.OdometerService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.*

class OdometerModelView : ViewModel(){
    val odometerDisplay = MutableLiveData<String>()
    var odometer: OdometerService? = null
    var bound = false
    val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(componentName: ComponentName, binder: IBinder) {

            if(binder is OdometerService.OdometerBinder){
                odometer = binder.getOdometer()
                odometer?.currentState = CurrentState.STATE_STOP
                bound = true
            }
        }
        override fun onServiceDisconnected(componentName: ComponentName) {
            bound = false
        }
    }

    fun startDisplayDistance(){
        GlobalScope.launch(Dispatchers.IO) {
            while (true){
                if( odometer != null) {
                    odometerDisplay.postValue( String.format(
                        Locale.getDefault()
                        ,"%1$,.2f meter"
                        , odometer?.getDistance()))
                }
                delay(1_000)
            }
        }
    }

    fun setState( state : CurrentState){
        odometer?.currentState = state
    }
}