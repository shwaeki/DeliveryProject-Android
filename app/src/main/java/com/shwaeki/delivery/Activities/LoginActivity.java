package com.shwaeki.delivery.Activities;


import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
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
import com.shwaeki.delivery.R;
import com.shwaeki.delivery.SharedPrefer;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class LoginActivity extends AppCompatActivity {
    private RequestQueue mRequestQueue;
    EditText Email, Password;
    SharedPrefer sharedPrefer;
    TextInputLayout emailInput, passwordlInput;
    Dialog dialog;

    @Override
    protected void onStart() {
        super.onStart();
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        mRequestQueue = Volley.newRequestQueue(this);
        Email = findViewById(R.id.editTextEmail);
        Password = findViewById(R.id.editTextPassword);
        emailInput = findViewById(R.id.textInputEmail);
        passwordlInput = findViewById(R.id.textInputPassword);

        sharedPrefer = new SharedPrefer(this);
        CheckUserPermsions();


        dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(false);
        dialog.setContentView(R.layout.load_dialog);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));

    }


    private void showMessageOKCancel(String message) {
        new AlertDialog.Builder(this)
                .setMessage(message)
                .setPositiveButton("Accept", (dialog, which) -> requestPermissions(new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, 123))
                .setNegativeButton("Reject", (dialog, which) -> finish())
                .create()
                .show();
    }

    void CheckUserPermsions() {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            showMessageOKCancel("We will need to access your location, you must accept the permissions to use the location");
            Log.i("Test", "Permission 1");
            return;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case 123:
                if (grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    finish();
                    Log.i("Test", "Permission 2");
                    Toast.makeText(LoginActivity.this, "Permission denied to access  your location.", Toast.LENGTH_SHORT).show();

                }
                break;
            default:
                Log.i("Test", "Permission 3");
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    public void Login(View view) {
        dialog.show();
        emailInput.setError(null);
        passwordlInput.setError(null);

        if (Email == null || Email.toString().trim().length() == 0) {
            emailInput.setError("Email is required");
            return;
        }
        if (Password == null || Password.toString().trim().length() == 0) {
            passwordlInput.setError("Password is required");
            return;
        }


        Map<String, String> params = new HashMap();
        params.put("email", Email.getText().toString().trim());
        params.put("password", Password.getText().toString().trim());
        JSONObject parameters = new JSONObject(params);

        JsonObjectRequest req = new JsonObjectRequest(Request.Method.POST, getString(R.string.appLink) + "login", parameters,
//        JsonObjectRequest req = new JsonObjectRequest(Request.Method.POST, AppInfo.AppLinkDelivery + "login", parameters,
                response -> {
                    try {
                        if (response.getString("status").equals("Success")) {
                            Intent intent = new Intent(LoginActivity.this, DeliveryMainActivity.class);
                            sharedPrefer.SaveStringData("access_token", "Bearer " + response.getJSONObject("data").getString("access_token"));
                            dialog.dismiss();
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            startActivity(intent);
                            finish();
                        } else {
                            DynamicToast.makeError(this, response.getString("message"), Toast.LENGTH_LONG).show();
                            dialog.dismiss();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        dialog.dismiss();
                    }
                    Log.i("Test YES", response.toString());
                }, error -> {
            Log.i("Test Error", error.toString());
            dialog.dismiss();
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