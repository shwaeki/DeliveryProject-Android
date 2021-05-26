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
import com.shwaeki.delivery.DeliveryInfo;
import com.shwaeki.delivery.R;
import com.shwaeki.delivery.SharedPrefer;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class SettingsActivity extends AppCompatActivity {
    private RequestQueue mRequestQueue;
    SharedPrefer sharedPrefer;
    EditText Name, OldPassword, NewPassword, ReNewPassword;
    TextInputLayout NameInput, OldPasswordeInput, NewPasswordeInput, ReNewPasswordeInput;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        mRequestQueue = Volley.newRequestQueue(this);
        sharedPrefer = new SharedPrefer(this);

        Name = findViewById(R.id.name);
        OldPassword = findViewById(R.id.old_password);
        NewPassword = findViewById(R.id.new_password);
        ReNewPassword = findViewById(R.id.renew_password);

        NameInput = findViewById(R.id.textInputname);
        OldPasswordeInput = findViewById(R.id.textInputoldpassword);
        NewPasswordeInput = findViewById(R.id.textInputnew_password);
        ReNewPasswordeInput = findViewById(R.id.textInputrenew_password);

        Name.setText(DeliveryInfo.Name);
    }

    //
    public void ChangeName(View view) {
        String NewName = Name.getText().toString().trim();


        if (NewName.length() < 6) {
            NameInput.setError("The New name must be more than 6 character!");
            return;
        }

        Map<String, String> params = new HashMap();
        params.put("name", NewName);
        JSONObject parameters = new JSONObject(params);
        Log.i("Test", NewName);
        JsonObjectRequest req = new JsonObjectRequest(Request.Method.PUT, getString(R.string.appLink) + "changeName", parameters,
//        JsonObjectRequest req = new JsonObjectRequest(Request.Method.PUT, AppInfo.AppLinkDelivery + "changeName", parameters,
                response -> {
                    try {
                        if (response.getString("status").equals("Success")) {
                            DynamicToast.makeSuccess(this, response.getString("message"),Toast.LENGTH_LONG).show();
                            finish();
                            Intent intent = new Intent(SettingsActivity.this, DeliveryMainActivity.class);
                            startActivity(intent);
                        }else{
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
                params.put("Authorization", sharedPrefer.LoadStringData("access_token"));
                return params;
            }
        };
        mRequestQueue.add(req);

    }

    public void ChangePass(View view) {
        String Old_Password = OldPassword.getText().toString().trim();
        String New_Password = NewPassword.getText().toString().trim();
        String ReNew_Password = ReNewPassword.getText().toString().trim();
        Log.i("Test", "Pass 1");
        OldPasswordeInput.setError(null);
        NewPasswordeInput.setError(null);
        ReNewPasswordeInput.setError(null);

        if (Old_Password.length() == 0) {
            OldPasswordeInput.setError("The Current Password can't be empty!");
            return;
        }
        if (New_Password.length() == 0) {
            NewPasswordeInput.setError("The New Password can't be empty!");
            return;
        }
        if (ReNew_Password.length() == 0) {
            ReNewPasswordeInput.setError("The Confirmation Password can't be empty!");
            return;
        }

        if (New_Password.length() < 6) {
            NewPasswordeInput.setError("The New Password must be more than 6 character!");
            return;
        }

        if (!New_Password.equals(ReNew_Password)) {
            ReNewPasswordeInput.setError("The new password does not match the confirmation password!");
            return;
        }
        Log.i("Test", "Pass 2");

        Map<String, String> params = new HashMap();
        params.put("current_password", Old_Password);
        params.put("password", New_Password);
        JSONObject parameters = new JSONObject(params);

//        JsonObjectRequest req = new JsonObjectRequest(Request.Method.PUT, getString(R.string.appLink) + "changePassword", parameters,
        JsonObjectRequest req = new JsonObjectRequest(Request.Method.PUT, AppInfo.AppLinkDelivery + "changePassword", parameters,
                response -> {
                    try {
                        Log.i("Test", "Pass 3");
                        if (response.getString("status").equals("Success")) {
                            DynamicToast.makeSuccess(this, response.getString("message"),Toast.LENGTH_LONG).show();
                            finish();
                            Intent intent = new Intent(SettingsActivity.this, DeliveryMainActivity.class);
                            startActivity(intent);
                        }else{
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
                params.put("Authorization", sharedPrefer.LoadStringData("access_token"));
                return params;
            }
        };
        mRequestQueue.add(req);

    }

    public void Back(View view) {
        this.onBackPressed();
    }
}