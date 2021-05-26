package com.shwaeki.delivery.Activities;


import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.content.pm.PackageManager;
import android.graphics.drawable.ColorDrawable;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.pranavpandey.android.dynamic.toasts.DynamicToast;
import com.shwaeki.delivery.AppInfo;
import com.shwaeki.delivery.DeliveryInfo;
import com.shwaeki.delivery.ClosingService;
import com.shwaeki.delivery.R;
import com.shwaeki.delivery.SharedPrefer;

import org.json.JSONException;

import java.util.HashMap;
import java.util.Map;

public class DeliveryMainActivity extends AppCompatActivity {
    RequestQueue mRequestQueue;
    SharedPrefer sharedPrefer;
    TextView username, profit;
    DatabaseReference myRef;
    Dialog dialog;

    @SuppressLint("MissingPermission")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_delivery);

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        myRef = database.getReference("DeliveryManLocations");

        if (!((LocationManager) getSystemService(LOCATION_SERVICE)).isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            showGPSAlert();
        }


        username = findViewById(R.id.username);
        profit = findViewById(R.id.profit);
        sharedPrefer = new SharedPrefer(this);
        mRequestQueue = Volley.newRequestQueue(this);

        dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(false);
        dialog.setContentView(R.layout.load_dialog);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        dialog.show();

        JsonObjectRequest req = new JsonObjectRequest(Request.Method.GET, getString(R.string.appLink) + "user", null,
//        JsonObjectRequest req = new JsonObjectRequest(Request.Method.GET, AppInfo.AppLinkDelivery + "user", null,
                response -> {
                    try {
                        int id = response.getInt("id");
                        String name = response.getString("name");
                        String email = response.getString("email");
                        DeliveryInfo.ID = id;
                        DeliveryInfo.Name = name;
                        DeliveryInfo.Email = email;
                        username.setText(name);
                        //      startService(new Intent(this, ClosingService.class));
                        if (CheckUserPermsions()) {
                            Thread thread = new Thread() {
                                @Override
                                public void run() {
                                    startService(new Intent(getApplicationContext(), ClosingService.class));
                                }
                            };
                            thread.start();
                            dialog.dismiss();
                        } else {
                            //      DynamicToast.makeError(this, "Please allow permission to access your location", Toast.LENGTH_LONG).show();
                            requestPermissions(new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, 123);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }, error -> {
            Log.i("Test Error", error.toString());
       /*     sharedPrefer.SaveStringData("access_token", null);
            Intent intent = new Intent(DeliveryMainActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();*/
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


        JsonObjectRequest req1 = new JsonObjectRequest(Request.Method.GET, getString(R.string.appLink) + "getTodayProfit", null,
//        JsonObjectRequest req1 = new JsonObjectRequest(Request.Method.GET, AppInfo.AppLinkDelivery + "getTodayProfit", null,
                response -> {
                    try {

                        if (response.getString("status").equals("Success")) {
                            int data = response.getInt("data");
                            profit.setText("₪" + data);
                            dialog.dismiss();
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }, error -> {
            Log.i("Test Error", error.toString());
        /*    sharedPrefer.SaveStringData("access_token", null);
            Intent intent = new Intent(DeliveryMainActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();*/
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
        mRequestQueue.add(req1);

    }


    private void showGPSAlert() {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setMessage("GPS is disabled in your device. Would you like to enable it?")
                .setCancelable(false)
                .setPositiveButton("Go to Settings To Enable GPS",
                        (dialog, id) -> {
                            Intent callGPSSettingIntent = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                            startActivity(callGPSSettingIntent);
                        });
        alertDialogBuilder.setNegativeButton("Cancel", (dialog, id) -> {
            dialog.cancel();
            finish();
        });
        AlertDialog alert = alertDialogBuilder.create();
        alert.show();
    }

    boolean CheckUserPermsions() {
        return ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case 123:
                if (grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    DynamicToast.makeError(this, "Permission denied to access your location.", Toast.LENGTH_LONG).show();
                    finish();
                } else {
                    Thread thread = new Thread() {
                        @Override
                        public void run() {
                            startService(new Intent(getApplicationContext(), ClosingService.class));
                        }
                    };
                    thread.start();
                }
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }


    public void logOut(View view) {
//        JsonObjectRequest req2 = new JsonObjectRequest(Request.Method.GET, getString(R.string.appLink) + "logout", null,
        JsonObjectRequest req2 = new JsonObjectRequest(Request.Method.GET, AppInfo.AppLinkDelivery + "logout", null,
                response -> {
                    try {
                        if (response.getString("status").equals("Success")) {
                            DynamicToast.makeSuccess(this, response.getString("message"), Toast.LENGTH_LONG).show();
                            sharedPrefer.SaveStringData("access_token", null);
                            Intent intent = new Intent(DeliveryMainActivity.this, MainActivity.class);
                            startActivity(intent);
                            finish();
                        } else {
                            DynamicToast.makeError(this, response.getString("message"), Toast.LENGTH_LONG).show();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }, error -> {
            Log.i("Test Error", error.toString());
            sharedPrefer.SaveStringData("access_token", null);
            Intent intent = new Intent(DeliveryMainActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
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
        mRequestQueue.add(req2);

    }

    public void GoToPackages(View view) {
        Intent intent = new Intent(DeliveryMainActivity.this, PackagesActivity.class);
        startActivity(intent);
    }

    public void GoToMap(View view) {
        Intent intent = new Intent(DeliveryMainActivity.this, MapActivity.class);
        startActivity(intent);
    }

    public void GoToSettings(View view) {
        Intent intent = new Intent(DeliveryMainActivity.this, SettingsActivity.class);
        startActivity(intent);
    }


    public void GoToProfit(View view) {
        Intent intent = new Intent(DeliveryMainActivity.this, ProfitActivity.class);
        startActivity(intent);
    }

    public void ScanQrCode(View view) {
        IntentIntegrator integrator = new IntentIntegrator(this);
        integrator.setPrompt("Scan a barcode");
        integrator.setBeepEnabled(false);
        integrator.setOrientationLocked(false);
        integrator.initiateScan();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (result != null) {
            if (result.getContents() == null) {
                DynamicToast.makeError(this, "Cancelled.", Toast.LENGTH_LONG).show();
            } else {

                String[] loca = result.getContents().trim().split(",");
                if (loca.length == 2) {
                    Uri mapUri = Uri.parse("geo:0,0?q=" + result.getContents().trim());
                    Intent mapIntent = new Intent(Intent.ACTION_VIEW, mapUri);
                    mapIntent.setPackage("com.google.android.apps.maps");
                    startActivity(mapIntent);
                }
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }


}
