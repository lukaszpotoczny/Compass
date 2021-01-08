package com.example.compass

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Looper
import android.provider.Settings
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.android.gms.location.*

class MainActivityViewModel : ViewModel(), SensorEventListener {
    private var sensorManager: SensorManager? = null
    private var context: Context? = null
    private var destination: Place? = null
    private var myLocation: Place? = null
    private var angleValue = 0f
    
    private val compassDegree = MutableLiveData<Float>()
    private val distance = MutableLiveData<Double>()
    private val angle = MutableLiveData<Float>()
    private val destinationDegree = MutableLiveData<Float>()

    private var fusedLocationProviderClient: FusedLocationProviderClient? = null
    private var locationRequest: LocationRequest? = null

    private var locationCallback: LocationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            if (locationResult == null) return
            for (location in locationResult.locations) {
                myLocation = Place(location.latitude, location.longitude)
                if (destination != null) {
                    distance.postValue(destination!!.meterDistanceBetweenPoints(myLocation!!.getLatitude(), myLocation!!.getLongitude()))
                    angleValue = destination!!.rotationAngle(myLocation!!.getLatitude(), myLocation!!.getLongitude())
                    destinationDegree.postValue(angleValue)
                    angle.postValue(angleValue)
                }
            }
        }
    }

    fun init(c: Context?) {
        context = c
        sensorManager = context!!.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(context!!)
        locationRequest = LocationRequest.create().setInterval(2000).setFastestInterval(1000).setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
    }

    fun getCompassDegree(): LiveData<Float> {
        return compassDegree
    }

    fun getDistance(): LiveData<Double> {
        return distance
    }

    fun getDestinationDegree(): LiveData<Float> {
        return destinationDegree
    }

    fun getAngle(): LiveData<Float> {
        return angle
    }

    fun setDestination(lat: Double, lng: Double) {
        destination = Place(lat, lng)
        if (myLocation != null) {
            distance.postValue(destination!!.meterDistanceBetweenPoints(myLocation!!.getLatitude(), myLocation!!.getLongitude()))
            angleValue = destination!!.rotationAngle(myLocation!!.getLatitude(), myLocation!!.getLongitude())
            destinationDegree.postValue(angleValue)
            angle.postValue(angleValue)
        }
    }

    fun registerSensor() {
        sensorManager!!.registerListener(this, sensorManager!!.getDefaultSensor(Sensor.TYPE_ORIENTATION), SensorManager.SENSOR_DELAY_FASTEST)
    }

    fun unRegisterSensor() {
        sensorManager!!.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent) {
        compassDegree.postValue(Math.round(event.values[0]).toFloat())
        if (destination != null) {
            destinationDegree.postValue(Math.round(event.values[0]).toFloat() + angleValue)
        }
    }

    override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {}
    fun setLocationUpdates() {
        val request = LocationSettingsRequest.Builder().addLocationRequest(locationRequest!!).build()
        val client = LocationServices.getSettingsClient(context!!)
        val locationSettingsResponseTask = client.checkLocationSettings(request)
        locationSettingsResponseTask.addOnSuccessListener { startLocationUpdates() }
        locationSettingsResponseTask.addOnFailureListener { e -> e.printStackTrace() }
    }

    @SuppressLint("MissingPermission")
    fun startLocationUpdates() {
        fusedLocationProviderClient!!.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper())
    }

    fun stopLocationUpdates() {
        fusedLocationProviderClient!!.removeLocationUpdates(locationCallback)
    }

    fun turnGPSOn() {
        val callGPSSettingIntent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
        context!!.startActivity(callGPSSettingIntent)
        setLocationUpdates()
    }
}