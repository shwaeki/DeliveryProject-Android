package com.shwaeki.delivery.Activities;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.shwaeki.delivery.AppInfo;
import com.shwaeki.delivery.R;
import com.shwaeki.delivery.SharedPrefer;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class PackageActivity extends AppCompatActivity {
    private RequestQueue mRequestQueue;
    private SharedPrefer sharedPrefer;
    TextView pname, pprice, pdelivery_cost, pstatus, cname, ccity, caddress, cphone, sname, scity, sphone;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_package);

        pname = findViewById(R.id.package_name);
        pprice = findViewById(R.id.package_price);
        pdelivery_cost = findViewById(R.id.package_delivery_cost);
        pstatus = findViewById(R.id.package_status);
        cname = findViewById(R.id.customer_name);
        ccity = findViewById(R.id.customer_city);
        caddress = findViewById(R.id.customer_address);
        cphone = findViewById(R.id.customer_phone);
        sname = findViewById(R.id.supplier_name);
        scity = findViewById(R.id.supplier_city);
        sphone = findViewById(R.id.supplier_phone);

        sharedPrefer = new SharedPrefer(this);
        mRequestQueue = Volley.newRequestQueue(this);

        Bundle b = getIntent().getExtras();

        String url = getString(R.string.appLink) + "getPackageInformation?package="+b.getInt("package");
//        String url = AppInfo.AppLinkDelivery+ "getPackageInformation?package="+b.getInt("package");
        Log.i("Test", "package = "+url);
        JsonObjectRequest req = new JsonObjectRequest(Request.Method.GET, url, null,
                response -> {
                    try {
                        JSONArray Data = response.getJSONArray("data");


                        JSONObject object = Data.getJSONObject(0);
                        int id = object.getInt("id");
                        String name = object.getString("name");
                        int price = object.getInt("price");
                        int delivery_cost = object.getInt("delivery_cost");
                        String status = object.getString("status");
                        pname.setText(name);
                        pprice.setText("₪"+price);
                        pdelivery_cost.setText("₪"+delivery_cost);
                        pstatus.setText(status);


                        JSONObject cInfo = object.getJSONObject("customer");
                        String ccname = cInfo.getString("name");
                        String cccity = cInfo.getString("City");
                        String ccaddress = cInfo.getString("Address");
                        String ccphone = cInfo.getString("Phone");
                        cname.setText(ccname);
                        ccity.setText(cccity);
                        caddress.setText(ccaddress);
                        cphone.setText(ccphone);


                        JSONObject sInfo = object.getJSONObject("supplier");
                        String ssname = cInfo.getString("name");
                        String sscity = cInfo.getString("City");
                        String ssphone = cInfo.getString("Phone");
                        sname.setText(ssname);
                        scity.setText(sscity);
                        sphone.setText(ssphone);


                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    Log.i("Test YES", response.toString());
                }
                , error -> {
            Log.i("Test Error", error.toString());
            //     sharedPrefer.SaveStringData("access_token", null);
            //    Intent intent = new Intent(this, LoginActivity.class);
            //   startActivity(intent);
            //    finish();
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