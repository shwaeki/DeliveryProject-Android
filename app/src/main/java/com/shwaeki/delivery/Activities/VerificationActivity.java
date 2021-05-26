package com.shwaeki.delivery.Activities;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Dialog;
import android.content.Intent;
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
import com.shwaeki.delivery.AppInfo;
import com.shwaeki.delivery.R;
import com.shwaeki.delivery.SharedPrefer;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class VerificationActivity extends AppCompatActivity {
    RequestQueue mRequestQueue;
    String TheCode, Phone;
    EditText code;
    TextInputLayout codeInput;
    SharedPrefer sharedPrefer;
    Dialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verification);

        sharedPrefer = new SharedPrefer(this);
        mRequestQueue = Volley.newRequestQueue(this);

        code = findViewById(R.id.editTextCode);
        codeInput = findViewById(R.id.textInputCode);
        TheCode = getIntent().getExtras().getString("Code");
        Phone = getIntent().getExtras().getString("Phone");

        dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(false);
        dialog.setContentView(R.layout.load_dialog);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));


    }

    public void CheckVerificationCode(View view) {
        dialog.show();
        codeInput.setError(null);

        if (!TheCode.equals(code.getText().toString().trim())) {
            codeInput.setError("Verification code does not match the code we sent!");
            dialog.dismiss();
            return;
        }


        Map<String, String> params = new HashMap();
        params.put("phone", Phone);
        params.put("verification_code", code.getText().toString().trim());
        JSONObject parameters = new JSONObject(params);

        JsonObjectRequest req = new JsonObjectRequest(Request.Method.POST, getString(R.string.appLinkCustomer) + "login", parameters,
//        JsonObjectRequest req = new JsonObjectRequest(Request.Method.POST, AppInfo.AppLinkCustomer + "login", parameters,
                response -> {
                    try {

                        if (response.getString("status").equals("Success")) {
                            sharedPrefer.SaveStringData("access_token_customer", "Bearer " + response.getJSONObject("data").getString("access_token"));
                            Intent intent = new Intent(VerificationActivity.this, CustomerMainActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            startActivity(intent);
                            dialog.dismiss();
                            finish();
                        } else {
                            Toast.makeText(VerificationActivity.this, response.getString("message"), Toast.LENGTH_SHORT).show();
                            dialog.dismiss();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        dialog.dismiss();
                    }
                    Log.i("Test YES", response.toString());
                }, error -> Log.i("Test Error", error.toString())) {
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