package com.example.compass;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.os.Looper;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import static android.content.Context.SENSOR_SERVICE;

public class MainActivityViewModel extends ViewModel implements SensorEventListener {

    private SensorManager sensorManager;
    private Context context;
    private Place destination;
    private Place myLocation;
    private float angleValue;

    private MutableLiveData<Float> compassDegree = new MutableLiveData<>();
    private MutableLiveData<Double> distance = new MutableLiveData<>();
    private MutableLiveData<Float> angle = new MutableLiveData<>();
    private MutableLiveData<Float> destinationDegree = new MutableLiveData<>();

    FusedLocationProviderClient fusedLocationProviderClient;
    LocationRequest locationRequest;
    LocationCallback locationCallback = new LocationCallback(){
        @Override
        public void onLocationResult(LocationResult locationResult) {
            if(locationResult == null) return;
            for(Location location : locationResult.getLocations()) {
                myLocation = new Place(location.getLatitude(), location.getLongitude());
                if(destination != null) {
                   distance.postValue(destination.meterDistanceBetweenPoints(myLocation.getLatitude(), myLocation.getLongitude()));
                   angleValue = destination.rotationAngle(myLocation.getLatitude(), myLocation.getLongitude());
                   destinationDegree.postValue(angleValue);
                   angle.postValue(angleValue);
               }
            }
        }
    };


    public void init(Context c){
        this.context = c;
        sensorManager = (SensorManager) context.getSystemService(SENSOR_SERVICE);
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(context);
        locationRequest = LocationRequest.create();
        locationRequest.setInterval(2000);
        locationRequest.setFastestInterval(1000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    public LiveData<Float> getCompassDegree(){
        return compassDegree;
    }

    public LiveData<Double> getDistance(){
        return distance;
    }

    public LiveData<Float> getDestinationDegree(){
        return destinationDegree;
    }

    public LiveData<Float> getAngle(){
        return angle;
    }

    public void setDestination(double lat, double lng){
        destination = new Place(lat, lng);
        if(myLocation != null){
            distance.postValue(destination.meterDistanceBetweenPoints(myLocation.getLatitude(), myLocation.getLongitude()));
            angleValue = destination.rotationAngle(myLocation.getLatitude(), myLocation.getLongitude());
            destinationDegree.postValue(angleValue);
            angle.postValue(angleValue);
        }
    }

    public void registerSensor(){
        sensorManager.registerListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION), SensorManager.SENSOR_DELAY_FASTEST);
    }

    public void unRegisterSensor(){
        sensorManager.unregisterListener(this);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        compassDegree.postValue((float)Math.round(event.values[0]));
        if(destination != null) {
            destinationDegree.postValue((float)Math.round(event.values[0]) + angleValue);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    public void setLocationUpdates(){
        LocationSettingsRequest request = new LocationSettingsRequest.Builder().addLocationRequest(locationRequest).build();
        SettingsClient client = LocationServices.getSettingsClient(context);
        Task<LocationSettingsResponse> locationSettingsResponseTask = client.checkLocationSettings(request);
        locationSettingsResponseTask.addOnSuccessListener(new OnSuccessListener<LocationSettingsResponse>() {
            @Override
            public void onSuccess(LocationSettingsResponse locationSettingsResponse) {
                startLocationUpdates();
            }
        });

        locationSettingsResponseTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                e.printStackTrace();
            }
        });
    }

    @SuppressLint("MissingPermission")
    public void startLocationUpdates(){
        fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper());
    }

    public void stopLocationUpdates(){
        fusedLocationProviderClient.removeLocationUpdates(locationCallback);
    }

    public void turnGPSOn(){
        Intent callGPSSettingIntent = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
        context.startActivity(callGPSSettingIntent);
        setLocationUpdates();
    }

}
