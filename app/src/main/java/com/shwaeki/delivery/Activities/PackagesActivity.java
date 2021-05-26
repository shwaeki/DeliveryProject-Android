package com.shwaeki.delivery.Activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.jaredrummler.materialspinner.MaterialSpinner;
import com.mapbox.mapboxsdk.Mapbox;
import com.shwaeki.delivery.Adapters.PackageAdapter;
import com.shwaeki.delivery.AppInfo;
import com.shwaeki.delivery.Models.Customer;
import com.shwaeki.delivery.Models.Package;
import com.shwaeki.delivery.R;
import com.shwaeki.delivery.SharedPrefer;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class PackagesActivity extends AppCompatActivity {
    private RequestQueue mRequestQueue;
    SharedPrefer sharedPrefer;
    ArrayList<Package> items, Fillteritems;
    PackageAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Mapbox.getInstance(this, getString(R.string.mapbox_access_token));
        setContentView(R.layout.activity_packages);

        sharedPrefer = new SharedPrefer(this);
        items = new ArrayList<Package>();
        Fillteritems = new ArrayList<Package>();
        RecyclerView recycler = findViewById(R.id.package_recycler);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);

        recycler.setLayoutManager(layoutManager);
        mAdapter = new PackageAdapter(this, Fillteritems, "DeliverMan");
        recycler.setAdapter(mAdapter);

        MaterialSpinner spinner = findViewById(R.id.spinner);
        spinner.setItems("Show All Packages", "Show Packages Undelivered Only", "Show Packages Delivered Only");
        spinner.setOnItemSelectedListener((view, position, id, item) -> {
            Log.i("Test", "Selected  index = " + position);
            FillterData(position);
        });


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
                            Fillteritems.add(new Package(id, name, price, delivery_cost, status, deliverd_at, SupplierID, created_at, customer));
                        }

                        mAdapter.notifyDataSetChanged();

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


    public void FillterData(int index) {
        Fillteritems.clear();
        for (int i = 0; i < items.size(); i++) {
            if (index == 0) {
                if (items.get(i).status.equals("Preparation") || items.get(i).status.equals("Delivered"))
                    Fillteritems.add(items.get(i));
            } else if (index == 1) {
                if (items.get(i).status.equals("Preparation"))
                    Fillteritems.add(items.get(i));
            } else if (index == 2) {
                if (items.get(i).status.equals("Delivered"))
                    Fillteritems.add(items.get(i));
            }

        }
        mAdapter.notifyDataSetChanged();
    }

    public void Back(View view) {
        this.onBackPressed();
    }
}
