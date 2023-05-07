package com.example.taller3.TareasAsync;



import android.content.Context;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Geocoder;
import android.os.AsyncTask;
import android.widget.Toast;

import androidx.core.content.ContextCompat;

import com.example.taller3.MapsActivity;
import com.example.taller3.R;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.List;


public class GeocoderTask extends AsyncTask<String, MapsActivity, LatLng> {

    private Context mContext;
    private GoogleMap googleMap;
    private MapView mapView;

    public GeocoderTask(Context context, GoogleMap googleMapIn, MapView mapViewIn) {
        mContext = context;
        googleMap=googleMapIn;
        mapView =mapViewIn;
    }

        @Override
        protected LatLng doInBackground(String... params) {

            Geocoder geocoder = new Geocoder(mContext);
            String address = params[0];
            try {
                List<Address> addresses = geocoder.getFromLocationName(address, 1);
                if (addresses.size() > 0) {
                    Address result = addresses.get(0);
                    double latitude = result.getLatitude();
                    double longitude = result.getLongitude();
                    return new LatLng(latitude, longitude);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }
        @Override
        protected void onPostExecute(LatLng result) {
            if (result != null) {
                if(googleMap!=null) {
                    googleMap.addMarker(new MarkerOptions().position(result).title("Marcador"));
                    googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(result, 15));
                }

            } else {
                Toast.makeText(mContext, "No se encontro el lugar ingresado", Toast.LENGTH_SHORT).show();
            }
        }
    }