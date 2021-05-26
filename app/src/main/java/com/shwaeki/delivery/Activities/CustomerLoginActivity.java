package com.shwaeki.delivery.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.textfield.TextInputLayout;
import com.pranavpandey.android.dynamic.toasts.DynamicToast;
import com.shwaeki.delivery.AppInfo;
import com.shwaeki.delivery.R;
import com.shwaeki.delivery.SharedPrefer;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class CustomerLoginActivity extends AppCompatActivity {
    RequestQueue mRequestQueue;
    SharedPrefer sharedPrefer;
    Spinner spinner;
    EditText phone;
    TextInputLayout phoneInput;
    Dialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_login);

        mRequestQueue = Volley.newRequestQueue(this);
        sharedPrefer = new SharedPrefer(this);

        spinner = findViewById(R.id.spinner_phone);
        phone = findViewById(R.id.editTextPhone);
        phoneInput = findViewById(R.id.textInputPhone);

        dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(false);
        dialog.setContentView(R.layout.load_dialog);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));

    }


    public void SendVerificationCode(View view) {
        dialog.show();
        phoneInput.setError(null);
        String phoneNumber = phone.getText().toString().trim();
        String FullPhoneNumber = spinner.getSelectedItem().toString() + phone.getText();
        // Toast.makeText(this, "Phone: " + spinner.getSelectedItem().toString() + phone.getText(), Toast.LENGTH_LONG).show();

        if (phoneNumber.length() != 10) {
            phoneInput.setError("The phone number must be 10 digits!");
            dialog.dismiss();
            return;
        }


        Map<String, String> params = new HashMap();
        params.put("phone", FullPhoneNumber);
        JSONObject parameters = new JSONObject(params);
        JsonObjectRequest req = new JsonObjectRequest(Request.Method.PUT, getString(R.string.appLinkCustomer) + "setCode", parameters,
//        JsonObjectRequest req = new JsonObjectRequest(Request.Method.PUT,   AppInfo.AppLinkCustomer + "setCode", parameters,
                response -> {
                    try {
                        if (response.getString("status").equals("Success")) {
                            DynamicToast.makeSuccess(this, response.getString("message"), Toast.LENGTH_LONG).show();
                            String Code = response.getString("data");
                            Intent intent = new Intent(this, VerificationActivity.class);
                            intent.putExtra("Code", Code);
                            intent.putExtra("Phone", FullPhoneNumber);
                            startActivity(intent);
                            dialog.dismiss();
                        } else {
                            phoneInput.setError(response.getString("message"));
                            DynamicToast.makeError(this, response.getString("message"), Toast.LENGTH_LONG).show();
                            dialog.dismiss();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        dialog.dismiss();
                    }
                    Log.i("Test YES", response.toString());
                }
                , error -> {
            dialog.dismiss();
            Log.i("Test Error", error.toString());
        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> params = new HashMap<String, String>();
                params.put("Content-Type", "application/json");
                params.put("X-Requested-With", "XMLHttpRequest");
                return params;
            }
        };
        mRequestQueue.add(req);


    }


    public void Back(View view) {
        this.onBackPressed();
    }
}
