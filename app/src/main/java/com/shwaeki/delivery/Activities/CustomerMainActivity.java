package com.shwaeki.delivery.Activities;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
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
import com.pranavpandey.android.dynamic.toasts.DynamicToast;
import com.shwaeki.delivery.AppInfo;
import com.shwaeki.delivery.CustomerInfo;
import com.shwaeki.delivery.R;
import com.shwaeki.delivery.SharedPrefer;

import org.json.JSONException;

import java.util.HashMap;
import java.util.Map;

public class CustomerMainActivity extends AppCompatActivity {
    RequestQueue mRequestQueue;
    SharedPrefer sharedPrefer;
    TextView username;
    Dialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_main);


        dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(false);
        dialog.setContentView(R.layout.load_dialog);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        dialog.show();

        username = findViewById(R.id.username);
        sharedPrefer = new SharedPrefer(this);
        mRequestQueue = Volley.newRequestQueue(this);


        JsonObjectRequest req = new JsonObjectRequest(Request.Method.GET, getString(R.string.appLinkCustomer) + "customer", null,
//        JsonObjectRequest req = new JsonObjectRequest(Request.Method.GET,   AppInfo.AppLinkCustomer  + "customer", null,
                response -> {
                    try {
                        int id = response.getInt("id");
                        CustomerInfo.ID = response.getInt("id");
                        CustomerInfo.Name = response.getString("name");
                        CustomerInfo.Phone = response.getString("Phone");
                        CustomerInfo.City = response.getString("City");
                        CustomerInfo.Address = response.getString("Address");
                        CustomerInfo.lat = Double.valueOf(response.getString("Latitude"));
                        CustomerInfo.lng = Double.valueOf(response.getString("Longitude"));
                        username.setText(response.getString("name"));
                        dialog.dismiss();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    Log.i("Test", response.toString());
                }, error -> {
            Log.i("Test Error", error.toString());
            sharedPrefer.SaveStringData("access_token_customer", null);
            Intent intent = new Intent(CustomerMainActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
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

    public void GoToMyPackages(View view) {
        Intent intent = new Intent(CustomerMainActivity.this, CustmerPackageActivity.class);
        startActivity(intent);
    }

    public void GoToChangeLocation(View view) {
        Intent intent = new Intent(CustomerMainActivity.this, CustomerMapActivity.class);
        startActivity(intent);
    }

    public void GoToSettings(View view) {
        Intent intent = new Intent(CustomerMainActivity.this, CustomerSettingsActivity.class);
        startActivity(intent);
    }

    public void logOut(View view) {
        JsonObjectRequest req2 = new JsonObjectRequest(Request.Method.GET, getString(R.string.appLinkCustomer) + "logout", null,
//        JsonObjectRequest req2 = new JsonObjectRequest(Request.Method.GET,   AppInfo.AppLinkCustomer + "logout", null,
                response -> {
                    try {
                        if (response.getString("status").equals("Success")) {
                            DynamicToast.makeSuccess(this, response.getString("message"), Toast.LENGTH_LONG).show();
                            sharedPrefer.SaveStringData("access_token_customer", null);
                            Intent intent = new Intent(CustomerMainActivity.this, MainActivity.class);
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
            sharedPrefer.SaveStringData("access_token_customer", null);
            Intent intent = new Intent(CustomerMainActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
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
        mRequestQueue.add(req2);

    }
}