
package com.shwaeki.delivery.Activities;

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
import com.shwaeki.delivery.Adapters.ProfitAdapter;
import com.shwaeki.delivery.AppInfo;
import com.shwaeki.delivery.Models.Profit;
import com.shwaeki.delivery.R;
import com.shwaeki.delivery.SharedPrefer;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ProfitActivity extends AppCompatActivity {
    private RequestQueue mRequestQueue;
    SharedPrefer sharedPrefer;
    ArrayList<Profit> items;
    ProfitAdapter mAdapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profit);

        MaterialSpinner spinner = findViewById(R.id.profit_spinner);
        spinner.setItems("Daily", "Monthly");
        spinner.setOnItemSelectedListener((view, position, id, item) -> {
            Log.i("Test", "Selected  index = " + position);
            UpdatePforig(position);
        });

        sharedPrefer = new SharedPrefer(this);
        items = new ArrayList<Profit>();
        RecyclerView recycler = findViewById(R.id.profit_recycler);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recycler.setLayoutManager(layoutManager);
        mAdapter = new ProfitAdapter(items);
        recycler.setAdapter(mAdapter);
        UpdatePforig(0);


    }

    void UpdatePforig(int type){
        items.clear();
        mRequestQueue = Volley.newRequestQueue(this);
        String url;
        if(type == 0) {
            url = getString(R.string.appLink) + "getAllProfits?method=Day";
//            url = AppInfo.AppLinkDelivery + "getAllProfits?method=Day";
        } else{
            url = getString(R.string.appLink) + "getAllProfits";
//            url = AppInfo.AppLinkDelivery + "getAllProfits";
        }


        JsonObjectRequest req = new JsonObjectRequest(Request.Method.GET, url, null,
                response -> {
                    try {
                        JSONArray Data = response.getJSONArray("data");
                        for (int i = 0; i < Data.length(); i++) {
                            JSONObject object = Data.getJSONObject(i);
                            String date;
                            if(type == 0) {
                                date = object.getString("date");
                            } else {
                                date = object.getString("year") + "-" + object.getString("month");
                            }
                            String total = object.getString("total");
                            String count = object.getString("count");
                            Log.i("Test",date);
                            items.add(new Profit(date, total, count));
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

    public void Back(View view) {
        this.onBackPressed();
    }
}
