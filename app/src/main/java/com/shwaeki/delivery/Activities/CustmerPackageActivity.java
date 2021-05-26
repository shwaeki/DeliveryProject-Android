package com.shwaeki.delivery.Activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.jaredrummler.materialspinner.MaterialSpinner;
import com.shwaeki.delivery.Adapters.PackageAdapter;
import com.shwaeki.delivery.AppInfo;
import com.shwaeki.delivery.Models.Customer;
import com.shwaeki.delivery.Models.Package;
import com.shwaeki.delivery.R;
import com.shwaeki.delivery.SharedPrefer;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class CustmerPackageActivity extends AppCompatActivity {
    private RequestQueue mRequestQueue;
    SharedPrefer sharedPrefer;
    ArrayList<Package> items, Fillteritems;
    PackageAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_custmer_package);


        MaterialSpinner spinner = findViewById(R.id.custmer_package_spinner);
        String[] statuses = getResources().getStringArray(R.array.statuses);

        spinner.setItems(statuses);
        spinner.setOnItemSelectedListener((view, position, id, item) -> {
            Log.i("Test", "Selected  index = " + position);
            changePackages(statuses[position]);
        });

        sharedPrefer = new SharedPrefer(this);
        items = new ArrayList<Package>();
        Fillteritems = new ArrayList<Package>();
        RecyclerView recycler = findViewById(R.id.customer_package_recycler);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recycler.setLayoutManager(layoutManager);
        mAdapter = new PackageAdapter(this, Fillteritems, "Customer");
        recycler.setAdapter(mAdapter);


        mRequestQueue = Volley.newRequestQueue(this);
        JsonObjectRequest req = new JsonObjectRequest(Request.Method.GET, getString(R.string.appLinkCustomer) + "getMyPackages", null,
//        JsonObjectRequest req = new JsonObjectRequest(Request.Method.GET,   AppInfo.AppLinkCustomer + "getMyPackages", null,
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


                            items.add(new Package(id, name, price, delivery_cost, status, deliverd_at, SupplierID, created_at));
                            Fillteritems.add(new Package(id, name, price, delivery_cost, status, deliverd_at, SupplierID, created_at));
                        }
                        Log.i("Test", "2");

                        mAdapter.notifyDataSetChanged();

                    } catch (JSONException e) {
                        Log.i("Test", "3");
                        e.printStackTrace();
                    }
                    Log.i("Test", "Package Count = " + items.size());
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

    public void changePackages(String status) {
        Fillteritems.clear();
        for (int i = 0; i < items.size(); i++) {
            if (status.equals("Received")) {
                if (items.get(i).status.equals("Received") || items.get(i).status.equals("ReceivedByCustomer"))
                    Fillteritems.add(items.get(i));
            } else if (items.get(i).status.equals(status) || status.equals("All Packages"))
                Fillteritems.add(items.get(i));
        }
        mAdapter.notifyDataSetChanged();
    }

}
