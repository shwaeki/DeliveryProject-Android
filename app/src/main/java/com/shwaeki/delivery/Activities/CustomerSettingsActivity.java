package com.shwaeki.delivery.Activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.textfield.TextInputLayout;
import com.pranavpandey.android.dynamic.toasts.DynamicToast;
import com.shwaeki.delivery.AppInfo;
import com.shwaeki.delivery.CustomerInfo;
import com.shwaeki.delivery.R;
import com.shwaeki.delivery.SharedPrefer;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class CustomerSettingsActivity extends AppCompatActivity {
    private RequestQueue mRequestQueue;
    SharedPrefer sharedPrefer;
    EditText Name, City, Phone, Address;
    TextInputLayout NameInput, CityInput, PhoneInput, AddressInput;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_settings);

        mRequestQueue = Volley.newRequestQueue(this);
        sharedPrefer = new SharedPrefer(this);

        Name = findViewById(R.id.name);
        // City = findViewById(R.id.ccity);
        Phone = findViewById(R.id.cphone);
        Address = findViewById(R.id.caddress);

        NameInput = findViewById(R.id.textInputname);
        // CityInput = findViewById(R.id.textInputCcity);
        PhoneInput = findViewById(R.id.textInputCphone);
        AddressInput = findViewById(R.id.textInputCaddress);

        Name.setText(CustomerInfo.Name);
        Phone.setText(CustomerInfo.Phone);
//        City.setText(CustomerInfo.City);
        Address.setText(CustomerInfo.Address);

    }


    public void ChangeInformation(View view) {
        NameInput.setError(null);
        //   CityInput.setError(null);
        PhoneInput.setError(null);
        AddressInput.setError(null);

        String NewName = Name.getText().toString().trim();
        //     String NewCity = City.getText().toString().trim();
        String NewPhone = Phone.getText().toString().trim();
        String NewAddress = Address.getText().toString().trim();


        if (NewName.length() < 6) {
            NameInput.setError("The new name must be more than 6 character!");
            return;
        }

        if (NewPhone.length() != 14) {
            PhoneInput.setError("The phone number must be with a county code [+970-059-0000-000]!");
            return;
        }
/*
        if (NewCity.length() <=1) {
            PhoneInput.setError("The city must me more then 2 character!");
            return;
        }*/

        if (NewAddress.length() <= 1) {
            PhoneInput.setError("The address must me more then 2 character!");
            return;
        }


        Map<String, String> params = new HashMap();
        params.put("name", NewName);
        //    params.put("city", NewCity);
        params.put("phone", NewPhone);
        params.put("address", NewAddress);
        JSONObject parameters = new JSONObject(params);
        JsonObjectRequest req = new JsonObjectRequest(Request.Method.PUT, getString(R.string.appLinkCustomer) + "changeInfo", parameters,
//        JsonObjectRequest req = new JsonObjectRequest(Request.Method.PUT,   AppInfo.AppLinkCustomer + "changeInfo", parameters,
                response -> {
                    try {
                        Toast.makeText(this, response.getString("message"), Toast.LENGTH_SHORT).show();
                        if (response.getString("status").equals("Success")) {
                            finish();
                            Intent intent = new Intent(CustomerSettingsActivity.this, CustomerMainActivity.class);
                            startActivity(intent);
                            DynamicToast.makeSuccess(this, response.getString("message"),Toast.LENGTH_LONG).show();
                        } else {
                            DynamicToast.makeError(this, response.getString("message"),Toast.LENGTH_LONG).show();
                        }
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
                params.put("Authorization", sharedPrefer.LoadStringData("access_token_customer"));
                return params;
            }
        };
        mRequestQueue.add(req);

    }

    public void Back(View view) {
        this.onBackPressed();
    }
}