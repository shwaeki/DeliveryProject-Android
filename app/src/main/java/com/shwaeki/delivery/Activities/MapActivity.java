package com.shwaeki.delivery.Activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Intent;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.jaredrummler.materialspinner.MaterialSpinner;
import com.mapbox.android.core.permissions.PermissionsManager;
import com.mapbox.api.directions.v5.models.DirectionsResponse;
import com.mapbox.api.directions.v5.models.DirectionsRoute;
import com.mapbox.geojson.Point;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.annotations.MarkerOptions;
import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.location.LocationComponent;
import com.mapbox.mapboxsdk.location.LocationComponentActivationOptions;
import com.mapbox.mapboxsdk.location.modes.CameraMode;
import com.mapbox.mapboxsdk.location.modes.RenderMode;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.maps.Style;
import com.mapbox.mapboxsdk.plugins.localization.LocalizationPlugin;
import com.mapbox.mapboxsdk.plugins.localization.MapLocale;
import com.mapbox.services.android.navigation.ui.v5.NavigationLauncher;
import com.mapbox.services.android.navigation.ui.v5.NavigationLauncherOptions;
import com.mapbox.services.android.navigation.ui.v5.route.NavigationMapRoute;
import com.mapbox.services.android.navigation.v5.navigation.NavigationRoute;
import com.pranavpandey.android.dynamic.toasts.DynamicToast;
import com.shwaeki.delivery.Adapters.MapPackageAdapter;
import com.shwaeki.delivery.DeliveryInfo;
import com.shwaeki.delivery.Models.Customer;
import com.shwaeki.delivery.Models.Package;
import com.shwaeki.delivery.R;
import com.shwaeki.delivery.SharedPrefer;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;

import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.textField;

public class MapActivity extends AppCompatActivity {
    private MapView mapView;
    MapboxMap mapbox;
    private RequestQueue mRequestQueue;
    SharedPrefer sharedPrefer;
    ArrayList<Package> items;
    MapPackageAdapter mAdapter;
    private DirectionsRoute currentRoute;
    private NavigationMapRoute navigationMapRoute;

    Location MyLocation;

    @SuppressLint("MissingPermission")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Mapbox.getInstance(this, getString(R.string.mapbox_access_token));
        setContentView(R.layout.activity_map);

        if (!((LocationManager) getSystemService(LOCATION_SERVICE)).isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            DynamicToast.makeError(this, "Please GPS is Enabled in your device!", Toast.LENGTH_SHORT).show();
            finish();
        }

        items = new ArrayList<Package>();
        sharedPrefer = new SharedPrefer(this);

        MaterialSpinner spinner = findViewById(R.id.spinner);
        spinner.setItems("Show All Packages", "Show Packages Undelivered Only", "Show Packages Delivered Only");
        spinner.setOnItemSelectedListener((view, position, id, item) -> {
            Log.i("Test", "Selected  index = " + position);
            addMarkares(position);
        });


        mapView = (MapView) findViewById(R.id.mapView);
        mapView.onCreate(savedInstanceState);


        MyLocation = getLocation();
        DeliveryInfo.lng = MyLocation.getLongitude();
        DeliveryInfo.lat = MyLocation.getLatitude();

        mapView.getMapAsync((OnMapReadyCallback) mapboxMap -> {
            mapbox = mapboxMap;
            mapboxMap.getUiSettings().setRotateGesturesEnabled(false);
            RecyclerView recycler = findViewById(R.id.map_packages_recycler);
            recycler.setLayoutManager(new LinearLayoutManager(MapActivity.this, LinearLayoutManager.HORIZONTAL, false));


            mAdapter = new MapPackageAdapter(MapActivity.this, items, mapboxMap, MyLocation);
            recycler.setAdapter(mAdapter);

            mapboxMap.setOnMarkerClickListener(marker -> {

                AlertDialog.Builder builder = new AlertDialog.Builder(MapActivity.this);
                LayoutInflater factory = LayoutInflater.from(MapActivity.this);
                final View view = factory.inflate(R.layout.map_dialog, null);
                builder.setView(view);
                AlertDialog dialog = builder.create();
                dialog.show();
                int id = getPackageById(Integer.parseInt(marker.getTitle()));

                double Lat = Double.parseDouble(items.get(id).customer.latitude);
                double Lng = Double.parseDouble(items.get(id).customer.longitude);

                Point originPoint = Point.fromLngLat(DeliveryInfo.lng, DeliveryInfo.lat);
                Point destinationPoint = Point.fromLngLat(Lng, Lat);
                getRoute(originPoint, destinationPoint);


                Log.i("Test ", "id : " + id);

                ((TextView) view.findViewById(R.id.map_name)).setText(items.get(id).name);
                ((TextView) view.findViewById(R.id.map_status)).setText(items.get(id).status);

                ((TextView) view.findViewById(R.id.map_phone)).setText(items.get(id).customer.phone);
                ((TextView) view.findViewById(R.id.map_price)).setText("₪" + items.get(id).price);
                ((TextView) view.findViewById(R.id.map_delivery_cost)).setText("₪" + items.get(id).delivery_cost);
                ((TextView) view.findViewById(R.id.map_cname)).setText(items.get(id).customer.name);
                if (items.get(id).status.equals("Delivered")) {
                    ((Button) view.findViewById(R.id.map_get_diriction)).setEnabled(false);
                    ((Button) view.findViewById(R.id.map_done)).setEnabled(false);
                } else if (items.get(id).status.equals("Preparation")) {
                    view.findViewById(R.id.map_get_diriction).setOnClickListener(v -> {

                        NavigationLauncherOptions options = NavigationLauncherOptions.builder()
                                .directionsRoute(currentRoute)
                                .shouldSimulateRoute(false)
                                .build();
                        NavigationLauncher.startNavigation(MapActivity.this, options);

                        /*
                        double lat = Double.parseDouble(items.get(id).customer.latitude);
                        double lon = Double.parseDouble(items.get(id).customer.longitude);
                        //Log.i("Test","geo:0,0?q=" + lat + "," + lon + "(" + items.get(id).customer.name + ")");
                        Uri mapUri = Uri.parse("geo:0,0?q=" + lat + "," + lon + "(" + items.get(id).customer.name + ")");
                        Intent mapIntent = new Intent(Intent.ACTION_VIEW, mapUri);
                        mapIntent.setPackage("com.google.android.apps.maps");
                        startActivity(mapIntent);*/
                    });

                    view.findViewById(R.id.map_done).setOnClickListener(v -> changeDeliveredStatus(items.get(id).id));
                }
                view.findViewById(R.id.map_information).setOnClickListener(v -> {
                    Log.i("Test", "Open Info");
                    Intent intent = new Intent(MapActivity.this, PackageActivity.class);
                    intent.putExtra("package", items.get(id).id);
                    startActivity(intent);
                });

                view.findViewById(R.id.map_close).setOnClickListener(v -> dialog.dismiss());
                return true;
            });

            mapboxMap.setStyle(Style.MAPBOX_STREETS, style -> {
                LocalizationPlugin localizationPlugin = new LocalizationPlugin(mapView, mapboxMap, style);
                localizationPlugin.setMapLanguage(MapLocale.ARABIC);

                // Show Device Location On The Map
                if (PermissionsManager.areLocationPermissionsGranted(MapActivity.this)) {
                    LocationComponent locationComponent = mapboxMap.getLocationComponent();
                    locationComponent.activateLocationComponent(LocationComponentActivationOptions.builder(MapActivity.this, style).build());
                    locationComponent.setLocationComponentEnabled(true);
                    locationComponent.setCameraMode(CameraMode.TRACKING);
                    locationComponent.setRenderMode(RenderMode.GPS);
                }
                getPackages();
            });

        });

    }

    public void getRoute(Point origin, Point destination) {
        NavigationRoute.builder(this)
                .accessToken(Mapbox.getAccessToken())
                .origin(origin)
                .destination(destination)
                .language(Locale.ENGLISH)
                .build()
                .getRoute(new Callback<DirectionsResponse>() {
                    @Override
                    public void onResponse(Call<DirectionsResponse> call, retrofit2.Response<DirectionsResponse> response) {
                        Log.d("Map", "Response code: " + response.code());
                        if (response.body() == null) {
                            Log.e("Map", "No routes found, make sure you set the right user and access token.");
                            return;
                        } else if (response.body().routes().size() < 1) {
                            Log.e("Map", "No routes found");
                            return;
                        }

                        currentRoute = response.body().routes().get(0);


                        if (navigationMapRoute != null) {
                            navigationMapRoute.removeRoute();
                        } else {
                            navigationMapRoute = new NavigationMapRoute(null, mapView, mapbox, R.style.NavigationMapRoute);
                        }
                        navigationMapRoute.addRoute(currentRoute);
                    }


                    @Override
                    public void onFailure(Call<DirectionsResponse> call, Throwable throwable) {
                        Log.e("Map", "Error: " + throwable.getMessage());
                    }
                });
    }

    void getPackages() {
        mRequestQueue = Volley.newRequestQueue(this);
        JsonObjectRequest req = new JsonObjectRequest(Request.Method.GET, getString(R.string.appLink) + "getPackages", null,
//        JsonObjectRequest req = new JsonObjectRequest(Request.Method.GET, AppInfo.AppLinkDelivery + "getPackages", null,
                response -> {
                    try {
                        JSONArray Data = response.getJSONArray("data");
                        for (int i = 0; i < Data.length(); i++) {
                            JSONObject object = Data.getJSONObject(i);
                            int id = object.getInt("id");
                            String name = object.getString("name");
                            int price = object.getInt("price");
                            int delivery_cost = object.getInt("delivery_cost");
                            String status = object.getString("status");
                            String deliverd_at = object.getString("deliverd_at");
                            String SupplierID = object.getString("SupplierID");
                            String created_at = object.getString("created_at");

                            JSONObject cInfo = object.getJSONObject("customer");
                            int cid = cInfo.getInt("id");
                            String cname = cInfo.getString("name");
                            String ccity = cInfo.getString("City");
                            String caddress = cInfo.getString("Address");
                            String cphone = cInfo.getString("Phone");
                            String clatitude = cInfo.getString("Latitude");
                            String clongitude = cInfo.getString("Longitude");
                            Customer customer = new Customer(cid, cname, ccity, caddress, cphone, clatitude, clongitude);
                            items.add(new Package(id, name, price, delivery_cost, status, deliverd_at, SupplierID, created_at, customer));
                        }
                        addMarkares(0);

                    } catch (JSONException e) {
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
                params.put("Authorization", sharedPrefer.LoadStringData("access_token"));
                return params;
            }
        };
        mRequestQueue.add(req);
    }

    void addMarkares(int type) {
        mapbox.clear();
        if (type == 0) {
            for (int i = 0; i < items.size(); i++) {
                if (items.get(i).status.equals("Preparation") || items.get(i).status.equals("Delivered")) {
                    mapbox.addMarker(new MarkerOptions()
                            .position(new LatLng(Double.parseDouble(items.get(i).customer.latitude), Double.parseDouble(items.get(i).customer.longitude)))
                            .title(String.valueOf(items.get(i).id)));

                }
            }
        } else if (type == 1) {
            for (int i = 0; i < items.size(); i++) {
                if (items.get(i).status.equals("Preparation")) {
                    mapbox.addMarker(new MarkerOptions()
                            .position(new LatLng(Double.parseDouble(items.get(i).customer.latitude), Double.parseDouble(items.get(i).customer.longitude)))
                            .title(String.valueOf(items.get(i).id)));

                }
            }
        } else if (type == 2) {
            for (int i = 0; i < items.size(); i++) {
                if (items.get(i).status.equals("Delivered")) {
                    mapbox.addMarker(new MarkerOptions()
                            .position(new LatLng(Double.parseDouble(items.get(i).customer.latitude), Double.parseDouble(items.get(i).customer.longitude)))
                            .title(String.valueOf(items.get(i).id)));

                }
            }
        }
        mAdapter.notifyDataSetChanged();
    }

    void changeDeliveredStatus(int id) {
        Log.i("Test ", "changeDeliveredStatus : " + id);
        Map<String, Integer> params = new HashMap();
        params.put("package", id);
        JSONObject parameters = new JSONObject(params);
        JsonObjectRequest req = new JsonObjectRequest(Request.Method.PUT, getString(R.string.appLink) + "updateDeliveryStatus", parameters,
//        JsonObjectRequest req = new JsonObjectRequest(Request.Method.PUT, AppInfo.AppLinkDelivery + "updateDeliveryStatus", parameters,
                response -> {
                    try {
                        if (response.getString("status").equals("Success")) {
                            DynamicToast.makeSuccess(this, response.getString("message"), Toast.LENGTH_LONG).show();
                        } else {
                            DynamicToast.makeError(this, response.getString("message"), Toast.LENGTH_LONG).show();
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    finish();
                    startActivity(getIntent());
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
                params.put("Authorization", sharedPrefer.LoadStringData("access_token"));
                return params;
            }
        };
        mRequestQueue.add(req);

    }

/*
    @SuppressLint("MissingPermission")
    public Location getLocation() {
        LocationManager mLocationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        return mLocationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
    }
*/

    @SuppressLint("MissingPermission")
    public Location getLocation()
    {
        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        Criteria criteria = new Criteria();
        String bestProvider = locationManager.getBestProvider(criteria, false);
        return locationManager.getLastKnownLocation(bestProvider);
    }


    public void getMyLocation(View view) {
        double Lat = getLocation().getLatitude();
        double Lng = getLocation().getLongitude();
        mapbox.setCameraPosition(new CameraPosition.Builder().target(new LatLng(Lat, Lng)).zoom(8.0).build());
    }

    int getPackageById(int id) {
        for (int i = 0; i < items.size(); i++) {
            if (items.get(i).id == id)
                return i;
        }
        return -1;
    }

    @Override
    protected void onStart() {
        super.onStart();
        mapView.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mapView.onStop();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }


}