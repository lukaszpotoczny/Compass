package com.example.compass;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;


public class MainActivity extends AppCompatActivity  {

    private ImageView imageCompass, imageDestination;
    private TextView textDistance, textDirection;
    private Button buttonDestination;
    private CardView cardCompass;

    private AlertDialog.Builder builder;
    private AlertDialog alertDialog;
    private EditText destLat, destLng;
    private Button buttonCancel, buttonConfirm;

    private float startDegreeCompass = 0f;
    private float startDegreeDestination = 0f;
    final int LOCATION_REQUEST_CODE = 10001;

    private MainActivityViewModel mainActivityViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        imageCompass = findViewById(R.id.imageCompass);
        imageDestination = findViewById(R.id.imageDestination);
        textDistance = findViewById(R.id.textMeters);
        textDirection = findViewById(R.id.textDirection);
        buttonDestination = findViewById(R.id.buttonCompass);
        cardCompass = findViewById(R.id.cardCompass);

        buttonDestination.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                checkPermissions();
            }
        });

        mainActivityViewModel = new ViewModelProvider(this).get(MainActivityViewModel.class);
        mainActivityViewModel.init(this);

        mainActivityViewModel.getCompassDegree().observe(this, new Observer<Float>() {
            @Override
            public void onChanged(Float aFloat) {
                animateCompass();
            }
        });

        mainActivityViewModel.getDistance().observe(this, new Observer<Double>() {
            @Override
            public void onChanged(Double aDouble) {
                setTextDestination();
            }
        });

        mainActivityViewModel.getDestinationDegree().observe(this, new Observer<Float>() {
            @Override
            public void onChanged(Float aFloat) {
                animateDestination();
            }
        });

        mainActivityViewModel.getAngle().observe(this, new Observer<Float>() {
            @Override
            public void onChanged(Float aFloat) {
                setTextDirection();
            }
        });

    }

    private void setTextDestination(){
        double dist = mainActivityViewModel.getDistance().getValue();
        int distance = (int)dist;
        if(distance < 10000) textDistance.setText("Distance from the destination: " + distance + "m");
        else textDistance.setText("Distance from the destination: " + distance / 1000 + "km");
    }

    private void setTextDirection(){
        float angle = mainActivityViewModel.getAngle().getValue() % 180;
        if(angle>-22.5 && angle<=22.5) textDirection.setText("Head: North");
        else if(angle>22.5 && angle<=67.5) textDirection.setText("Head: North-West");
        else if(angle>67.5 && angle<=112.5) textDirection.setText("Head: West");
        else if(angle>112.5 && angle<=157.5) textDirection.setText("Head: South-West");
        else if(angle>157.5 || angle<=-157.5) textDirection.setText("Head: South");
        else if(angle>-157.5 && angle<=-112.5) textDirection.setText("Head: South-East");
        else if(angle>-112.5 && angle<=-67.5) textDirection.setText("Head: East");
        else if(angle>-67.5 && angle<=-22.5) textDirection.setText("Head: North-East");
    }

    private void animateCompass(){
        float degree = mainActivityViewModel.getCompassDegree().getValue();
        RotateAnimation ra = new RotateAnimation(
                startDegreeCompass,
                -degree,
                Animation.RELATIVE_TO_SELF, 0.5f,
                Animation.RELATIVE_TO_SELF, 0.5f);
        ra.setFillAfter(true);
        ra.setDuration(500);
        imageCompass.startAnimation(ra);
        startDegreeCompass = -degree;
    }

    private void animateDestination(){
        float degree = mainActivityViewModel.getDestinationDegree().getValue();
        RotateAnimation ra = new RotateAnimation(
                startDegreeDestination,
                -degree,
                Animation.RELATIVE_TO_SELF, 0.5f,
                Animation.RELATIVE_TO_SELF, 0.5f);
        ra.setFillAfter(true);
        ra.setDuration(500);
        imageDestination.startAnimation(ra);
        startDegreeDestination = -degree;
    }

    @Override
    protected void onStart() {
        super.onStart();
        if(ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
            mainActivityViewModel.setLocationUpdates();
        }
        else{
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_REQUEST_CODE);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        mainActivityViewModel.unRegisterSensor();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mainActivityViewModel.registerSensor();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mainActivityViewModel.stopLocationUpdates();
    }

    private void checkPermissions(){
        if(ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
            if(checkGPSConnection()){
                mainActivityViewModel.setLocationUpdates();
                setDestinationDialog();
            }
            else buildAlertMessageNoGps();
        }
        else{
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if(requestCode == LOCATION_REQUEST_CODE){
            if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                mainActivityViewModel.setLocationUpdates();
            }
        }
    }

    private boolean checkGPSConnection(){
        final LocationManager manager = (LocationManager) getSystemService( Context.LOCATION_SERVICE );
        return manager.isProviderEnabled( LocationManager.GPS_PROVIDER);
    }

    private void buildAlertMessageNoGps() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Your GPS seems to be disabled, do you want to enable it?")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, final int id) {
                        mainActivityViewModel.turnGPSOn();
                        dialog.dismiss();
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, final int id) {
                        dialog.cancel();
                    }
                });
        final AlertDialog alert = builder.create();
        alert.show();
    }

    private void setDestinationDialog(){
        builder = new AlertDialog.Builder(this);
        final View popupView = getLayoutInflater().inflate(R.layout.destination_popup, null);
        destLat = popupView.findViewById(R.id.editLat);
        destLng = popupView.findViewById(R.id.editLng);
        buttonCancel = popupView.findViewById(R.id.buttonCancel);
        buttonConfirm = popupView.findViewById(R.id.buttonConfirm);

        builder.setView(popupView);
        alertDialog = builder.create();
        alertDialog.show();

        buttonCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                alertDialog.dismiss();
            }
        });

        buttonConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(destLat.getText().toString().equals("") || destLng.getText().toString().equals("")){
                    Toast.makeText(MainActivity.this, "Insert correct values", Toast.LENGTH_SHORT).show();
                }
                else {
                    double lat = Double.parseDouble(destLat.getText().toString());
                    double lng = Double.parseDouble(destLng.getText().toString());
                    if(lat>90 || lat<-90 || lng>180 || lng<-180) {
                        Toast.makeText(MainActivity.this, "Insert correct values", Toast.LENGTH_SHORT).show();
                    }
                    else{
                        mainActivityViewModel.setDestination(lat, lng);
                        imageDestination.setVisibility(View.VISIBLE);
                        cardCompass.setVisibility(View.VISIBLE);
                        alertDialog.dismiss();
                    }
                }
            }
        });
    }


}