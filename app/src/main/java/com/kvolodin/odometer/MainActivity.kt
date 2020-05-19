package com.kvolodin.odometer

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.kvolodin.odometer.modelView.OdometerModelView
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


class MainActivity : AppCompatActivity() {
    private var bound = false
    private lateinit var activityViewModel: OdometerModelView
    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(componentName: ComponentName, binder: IBinder) {

            if(binder is OdometerService.OdometerBinder){
                activityViewModel.odometer = binder.getOdometer()
                bound = true
            }
        }

        override fun onServiceDisconnected(componentName: ComponentName) {
            bound = false
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // create service
        bindService(
            Intent(this,OdometerService::class.java)
            ,serviceConnection
            ,Context.BIND_AUTO_CREATE)

        // viewModel
        activityViewModel = ViewModelProviders.of(this).get(OdometerModelView::class.java)
        activityViewModel.odometerDisplay.observe(this,
            Observer { distance.text = it } );
        activityViewModel.startDisplayDistance()
    }

    override fun onDestroy() {
        super.onDestroy()

        if(bound){
            unbindService(serviceConnection)
            bound = false
        }
    }
}
