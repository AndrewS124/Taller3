package com.example.taller3.TareasAsync;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.os.AsyncTask;

import androidx.annotation.NonNull;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;


import java.io.IOException;
import java.util.List;

public class GetMarketAsyncTask extends AsyncTask<Double, Void, MarkerOptions> {

    private Context mContext;
    private GoogleMap googleMap;
    private MapView mapView;

    public GetMarketAsyncTask(Context context, GoogleMap googleMapIn, MapView mapViewIn) {
        mContext = context;
        googleMap=googleMapIn;
        mapView =mapViewIn;
    }

    @Override
        protected MarkerOptions doInBackground(@NonNull Double... params) {

            Geocoder geocoder = new Geocoder(mContext);
            Double latitude = params[0];
            Double longitude = params[1];

            List<Address> addresses = null;
            LatLng latLng = null;
            MarkerOptions markerOptions =null;
            try {
                addresses = geocoder.getFromLocation( latitude, longitude, 1);
                Address address = addresses.get(0);
                latLng = new LatLng(latitude, longitude);
                markerOptions = new MarkerOptions().position(latLng).title(address.getAddressLine(0));

            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            return markerOptions;
        }
    }