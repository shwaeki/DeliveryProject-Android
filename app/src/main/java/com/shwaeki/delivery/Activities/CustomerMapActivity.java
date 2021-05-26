package com.shwaeki.delivery.Activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.annotations.IconFactory;
import com.mapbox.mapboxsdk.annotations.Marker;
import com.mapbox.mapboxsdk.annotations.MarkerOptions;
import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.Style;
import com.pranavpandey.android.dynamic.toasts.DynamicToast;
import com.shwaeki.delivery.Adapters.ProfitAdapter;
import com.shwaeki.delivery.AppInfo;
import com.shwaeki.delivery.CustomerInfo;
import com.shwaeki.delivery.R;
import com.shwaeki.delivery.SharedPrefer;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class CustomerMapActivity extends AppCompatActivity {
    MapView mapView;
    MapboxMap mapbox;
    RequestQueue mRequestQueue;
    SharedPrefer sharedPrefer;
    Marker CurrentMarker;

    @SuppressLint("MissingPermission")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Mapbox.getInstance(this, getString(R.string.mapbox_access_token));
        setContentView(R.layout.activity_customer_map);

        if (!((LocationManager) getSystemService(LOCATION_SERVICE)).isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            DynamicToast.makeError(this, "Please GPS is Enabled in your device!", Toast.LENGTH_SHORT).show();
            finish();
        }


        mRequestQueue = Volley.newRequestQueue(this);

        mapView = (MapView) findViewById(R.id.mapView);
        mapView.onCreate(savedInstanceState);

        sharedPrefer = new SharedPrefer(this);
        mapView.getMapAsync(mapboxMap -> {
            mapbox = mapboxMap;


            mapboxMap.addOnMapClickListener(point -> {
                CurrentMarker.setPosition(point);
                Log.i("Test", "point =" + point.toString());
                return true;
            });


            mapboxMap.setStyle(Style.MAPBOX_STREETS, style -> {
                double Lat = CustomerInfo.lat;
                double Lng = CustomerInfo.lng;

                mapboxMap.setCameraPosition(new CameraPosition.Builder().target(new LatLng(Lat, Lng)).zoom(8.0).build());
                CurrentMarker = mapbox.addMarker(new MarkerOptions().position(new LatLng(Lat, Lng)).title("Current Location"));
            });
        });

    }

    private void showMessageOKCancel(String message) {
        new AlertDialog.Builder(this)
                .setMessage(message)
                .setPositiveButton("Accept", (dialog, which) -> requestPermissions(new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, 123))
                .setNegativeButton("Reject", (dialog, which) -> finish())
                .create()
                .show();
    }

    boolean CheckUserPermsions() {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            showMessageOKCancel("We will need to access your location, you must accept the permissions to use the location");
            return false;
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case 123:
                if (grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    finish();

                    DynamicToast.makeError(this, "Permission denied to access  your location.", Toast.LENGTH_LONG).show();

                } else {
                    double Lat = getLocation().getLatitude();
                    double Lng = getLocation().getLongitude();
                    mapbox.setCameraPosition(new CameraPosition.Builder().target(new LatLng(Lat, Lng)).zoom(8.0).build());
                    CurrentMarker.setPosition(new LatLng(Lat, Lng));
                }
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }


    @SuppressLint("MissingPermission")
    Location getLocation() {
        LocationManager mLocationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        return mLocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
    }

    public void getMyLocation(View view) {
        if (CheckUserPermsions()) {
            double Lat = getLocation().getLatitude();
            double Lng = getLocation().getLongitude();
            mapbox.setCameraPosition(new CameraPosition.Builder().target(new LatLng(Lat, Lng)).zoom(8.0).build());
            CurrentMarker.setPosition(new LatLng(Lat, Lng));
        }
    }


    public void ChangeLocation(View view) {
        Map<String, Double> params = new HashMap();
        params.put("Latitude", CurrentMarker.getPosition().getLatitude());
        params.put("Longitude", CurrentMarker.getPosition().getLongitude());
        JSONObject parameters = new JSONObject(params);
        JsonObjectRequest req = new JsonObjectRequest(Request.Method.PUT, getString(R.string.appLinkCustomer) + "changeLocation", parameters,
//        JsonObjectRequest req = new JsonObjectRequest(Request.Method.PUT,   AppInfo.AppLinkCustomer + "changeLocation", parameters,
                response -> {

                    try {
                        if (response.getString("status").equals("Success")) {
                            finish();
                            DynamicToast.makeSuccess(this, response.getString("message"),Toast.LENGTH_LONG).show();
                            Intent intent = new Intent(CustomerMapActivity.this, CustomerMainActivity.class);
                            startActivity(intent);
                        }else {
                            DynamicToast.makeError(this, response.getString("message"),Toast.LENGTH_LONG).show();
                        }
                    } catch (JSONException e) {
                        Log.i("Test", "3");
                        e.printStackTrace();
                    }
                    Log.i("Test YES", response.toString());
                }
                , error -> {
            Log.i("Test Error", error.toString());
        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> params = new HashMap<String, String>();
                params.put("Content-Type", "application/json");
                params.put("X-Requested-With", "XMLHttpRequest");
                params.put("Authorization", sharedPrefer.LoadStringData("access_token_customer"));
                return params;
            }
        };
        mRequestQueue.add(req);

    }


}