package com.example.taller3;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;


import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.example.taller3.TareasAsync.GetMarketAsyncTask;
import com.example.taller3.databinding.ActivityMapsBinding;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;


import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutionException;

import android.Manifest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    static final int PERMISSIONS_REQUEST_LOCATION = 0;
    static final double RADIUS_OF_EARTH_KM = 6371;
    SensorManager sensorManager;
    Sensor lightSensor;
    SensorEventListener lightSensorListener;
    private static GoogleMap mMap;
    MapView mapView;

    ActivityMapsBinding binding;
    private FusedLocationProviderClient fusedLocationClient;
    private LocationRequest locationRequest;
    private LocationCallback locationCallback;
    private double latitudeUbi;
    private double longitudeUbi;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMapsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        //Inicializar el objeto GoogleMap
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.mapView);
        assert mapFragment != null;
        mapFragment.getMapAsync(this);

        locationRequest = createLocationRequest();

        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);



        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.getLastLocation().addOnSuccessListener(new OnSuccessListener<Location>() {
                @Override
                public void onSuccess(Location location) {
                    if (location != null) {
                        latitudeUbi = location.getLatitude();
                        longitudeUbi = location.getLongitude();
                        GetMarketAsyncTask a = new GetMarketAsyncTask(getBaseContext(),  mMap,  mapView);
                        a.execute(latitudeUbi, longitudeUbi);
                        LatLng lastUbi = new LatLng(latitudeUbi, longitudeUbi);



                        try {
                            //mMap.clear();
                            mMap.addMarker(a.get());
                            mMap.moveCamera(CameraUpdateFactory.newLatLng(lastUbi));
                        } catch (ExecutionException | InterruptedException e) {
                            throw new RuntimeException(e);
                        }

                    }
                }
            });
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSIONS_REQUEST_LOCATION);
        }


        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                Location location = locationResult.getLastLocation();

                double latitudeLast = location.getLatitude();
                double longitudeLast = location.getLongitude();
                GetMarketAsyncTask a = new GetMarketAsyncTask(getBaseContext(),  mMap,  mapView);

                if ( Distancia.distance(latitudeLast, longitudeLast, latitudeUbi, longitudeUbi ) > 0.3){

                    a.execute(latitudeLast, longitudeLast);
                    LatLng lastUbi = new LatLng(latitudeLast, longitudeLast);

                    try {
                        mMap.clear();
                        mMap.addMarker(a.get());
                        mMap.moveCamera(CameraUpdateFactory.newLatLng(lastUbi));
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }

                    latitudeUbi = latitudeLast;
                    longitudeUbi = longitudeLast;

                }

                Log.i("LOCATION", "Location update in the callback: " + location);
            }
        };


    }

    @NonNull
    private LocationRequest createLocationRequest() {

        return LocationRequest.create()
                .setInterval(10000)
                .setFastestInterval(5000)
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);


    }

    private void startLocationUpdates() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.requestLocationUpdates( locationRequest, locationCallback, null);
        }
    }

    //Todas las modificaciones al mapa
    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {

        mMap = googleMap;
        try {
            InputStream is = getAssets().open("locations.json");
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            String json = new String(buffer, StandardCharsets.UTF_8);

            JSONObject jsonObject = new JSONObject(json);
            JSONArray jsonArray = jsonObject.getJSONArray("locationsArray");

            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject locationObject = jsonArray.getJSONObject(i);
                double latitude = locationObject.getDouble("latitude");
                double longitude = locationObject.getDouble("longitude");
                String name = locationObject.getString("name");
                LatLng latLng = new LatLng(latitude, longitude);
                mMap.addMarker(new MarkerOptions().position(latLng).title(name));
            }

        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }

    }


    @Override
    protected void onResume() {
        super.onResume();
        sensorManager.registerListener(lightSensorListener,lightSensor,SensorManager.SENSOR_DELAY_NORMAL);
        startLocationUpdates();
    }

    @Override
    protected void onPause(){
        super.onPause();
        sensorManager.unregisterListener(lightSensorListener);
    }



}

