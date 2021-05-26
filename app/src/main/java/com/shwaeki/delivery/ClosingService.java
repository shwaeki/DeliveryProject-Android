package com.shwaeki.delivery;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import androidx.annotation.Nullable;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

public class ClosingService extends Service {
    SharedPrefer sharedPrefer;
    DatabaseReference myRef;

    @SuppressLint("MissingPermission")
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        myRef = database.getReference("DeliveryManLocations");

        sharedPrefer = new SharedPrefer(this);


        LocationListener locationListener = new android.location.LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                double latitude = location.getLatitude();
                double longitude = location.getLongitude();
                DatabaseReference usersRef = myRef.child(String.valueOf(DeliveryInfo.ID));
                Map<String, String> map = new HashMap<>();
                map.put("firstName", DeliveryInfo.Name);
                map.put("Longitude", String.valueOf(longitude));
                map.put("Latitude", String.valueOf(latitude));
                map.put("Status", "OnLine");
                usersRef.setValue(map);
                DeliveryInfo.lat=latitude;
                DeliveryInfo.lng=longitude;
                Log.i("Test", "latitude: " + latitude + ", longitude:" + longitude);
            }

            @Override
            public void onStatusChanged(String s, int i, Bundle bundle) {
            }

            @Override
            public void onProviderEnabled(String s) {
            }

            @Override
            public void onProviderDisabled(String s) {
            }
        };
        LocationManager locationManager = (LocationManager) this.getSystemService(LOCATION_SERVICE);
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 10000, 0, locationListener);

        return super.onStartCommand(intent, flags, startId);

    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        super.onTaskRemoved(rootIntent);
        DatabaseReference usersRef = myRef.child(String.valueOf(DeliveryInfo.ID));
        usersRef.removeValue();

        Log.i("Test", "App Is Closed");

        // Destroy the service
        stopSelf();
    }
}