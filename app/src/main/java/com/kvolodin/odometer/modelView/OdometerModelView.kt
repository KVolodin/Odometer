package com.kvolodin.odometer.modelView

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.kvolodin.odometer.OdometerService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.*


class OdometerModelView() : ViewModel(){
    val odometerDisplay = MutableLiveData<String>()
    var odometer: OdometerService? = null

    fun startDisplayDistance(){
        GlobalScope.launch(Dispatchers.IO) {
            while (true){
                if(odometer != null) {
                    odometerDisplay.postValue( String.format(
                        Locale.getDefault()
                        ,"%1$,.2f meter"
                        , odometer?.getDistance()))
                }
                delay(1_000)
            }
        }
    }
}