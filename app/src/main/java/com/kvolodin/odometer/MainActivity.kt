package com.kvolodin.odometer

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.IBinder
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.kvolodin.odometer.modelView.OdometerModelView
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity() {
    private val PERMISSION_REQUEST_CODE = 698
    private lateinit var activityViewModel: OdometerModelView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if ( ContextCompat.checkSelfPermission(this, OdometerService.PERMISSION_STRING) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(OdometerService.PERMISSION_STRING),PERMISSION_REQUEST_CODE);
        } else {
            initialViewModelAndBindService()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        when (requestCode) {
            PERMISSION_REQUEST_CODE -> {
                if (grantResults.isNotEmpty()
                    && grantResults[0] === PackageManager.PERMISSION_GRANTED
                ) {
                    initialViewModelAndBindService()
                } else {
                    Toast.makeText(this,"You didn't provide access to location",Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun initialViewModelAndBindService (){
        // viewModel
        activityViewModel = ViewModelProviders.of(this).get(OdometerModelView::class.java)
        activityViewModel.odometerDisplay.observe(this, Observer { distance.text = it });
        b_start.setOnClickListener {
            activityViewModel.setState(CurrentState.STATE_START)
            b_start.isEnabled = false
            b_stop.isEnabled = true  }
        b_stop.setOnClickListener {
            activityViewModel.setState(CurrentState.STATE_STOP);
            b_stop.isEnabled = false
            b_start.isEnabled = true }
        b_stop.isEnabled = false
        b_restart.setOnClickListener { activityViewModel.setState(CurrentState.STATE_RESTART)  }
        activityViewModel.startDisplayDistance()

        // create service
        bindService(
            Intent(this, OdometerService::class.java)
            , activityViewModel.serviceConnection
            , Context.BIND_AUTO_CREATE
        )
    }

    override fun onDestroy() {
        super.onDestroy()

        if(activityViewModel.bound){
            unbindService(activityViewModel.serviceConnection)
            activityViewModel.bound = false
        }
    }
}
