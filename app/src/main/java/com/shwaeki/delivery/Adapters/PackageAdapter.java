package com.shwaeki.delivery.Adapters;


import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RatingBar;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.mapbox.api.directions.v5.models.DirectionsResponse;
import com.mapbox.api.directions.v5.models.DirectionsRoute;
import com.mapbox.geojson.Point;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.services.android.navigation.ui.v5.NavigationLauncher;
import com.mapbox.services.android.navigation.ui.v5.NavigationLauncherOptions;
import com.mapbox.services.android.navigation.ui.v5.route.NavigationMapRoute;
import com.mapbox.services.android.navigation.v5.navigation.NavigationRoute;
import com.pranavpandey.android.dynamic.toasts.DynamicToast;
import com.shwaeki.delivery.Activities.MapActivity;
import com.shwaeki.delivery.Activities.PackageActivity;
import com.shwaeki.delivery.Activities.VerificationActivity;
import com.shwaeki.delivery.AppInfo;
import com.shwaeki.delivery.R;
import com.shwaeki.delivery.Models.Package;
import com.shwaeki.delivery.SharedPrefer;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;


public class PackageAdapter extends RecyclerView.Adapter<PackageAdapter.ViewHolder> {
    private final ArrayList<Package> values;
    private final Context context;
    private final String userType;
    private final RequestQueue mRequestQueue;
    private final SharedPrefer sharedPrefer;
    private Dialog dialog;


    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public TextView name, price, delivery_cost, status, customer, phone, time;
        public Button getDirction, done;
        public View layout;
        public TableRow timeRow;


        public ViewHolder(View v) {
            super(v);
            layout = v;
            layout.setOnClickListener(this);
            name = v.findViewById(R.id.pname);
            price = v.findViewById(R.id.price);
            delivery_cost = v.findViewById(R.id.delivery_cost);
            status = v.findViewById(R.id.status);
            customer = v.findViewById(R.id.customer);
            getDirction = v.findViewById(R.id.get_diriction);
            done = v.findViewById(R.id.done);
            phone = v.findViewById(R.id.phone);
            timeRow = v.findViewById(R.id.timeRow);
            time = v.findViewById(R.id.time);

        }

        @Override
        public void onClick(View v) {
            if (userType.equals("DeliverMan")) {
                int position = getLayoutPosition();
                Toast.makeText(context, "name = " + values.get(position).name, Toast.LENGTH_SHORT).show();

                Intent intent = new Intent(context, PackageActivity.class);
                intent.putExtra("package", values.get(position).id);
                context.startActivity(intent);
            }
        }
    }


    public PackageAdapter(Context context, ArrayList<Package> values, String userType) {
        mRequestQueue = Volley.newRequestQueue(context);
        sharedPrefer = new SharedPrefer(context);
        this.values = values;
        this.context = context;
        this.userType = userType;
        this.dialog = new Dialog(context);
        this.dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.dialog.setCancelable(false);
        this.dialog.setContentView(R.layout.load_dialog);
        this.dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
    }

    @Override
    public PackageAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View v;
        if (userType.equals("DeliverMan"))
            v = inflater.inflate(R.layout.package_row, parent, false);
        else
            v = inflater.inflate(R.layout.customer_package_row, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {

        Package pack = values.get(position);
        holder.name.setText(pack.name);
        holder.price.setText("₪" + pack.price);
        holder.delivery_cost.setText("₪" + pack.delivery_cost);
        holder.status.setText(pack.status);


        if (userType.equals("DeliverMan")) {
            holder.getDirction.setEnabled(true);
            holder.done.setEnabled(true);

            holder.customer.setText(pack.customer.name);
            holder.phone.setText(pack.customer.phone);

            if ((pack.status.equals("Delivered"))) {
                holder.getDirction.setEnabled(false);
                holder.done.setEnabled(false);
            } else if (pack.status.equals("Preparation")) {
                holder.getDirction.setOnClickListener(v -> {
                    double lat = Double.parseDouble(pack.customer.latitude);
                    double lon = Double.parseDouble(pack.customer.longitude);
                    dialog.show();

                    Location location1 = getLocation();
                    Point originPoint = Point.fromLngLat(location1.getLongitude(), location1.getLatitude());
                    Point destinationPoint = Point.fromLngLat(lon, lat);
                    getRoute(originPoint, destinationPoint);

                    /*
                    Uri mapUri = Uri.parse("geo:0,0?q=" + lat + "," + lon + "(" + pack.customer.name + ")");
                    Intent mapIntent = new Intent(Intent.ACTION_VIEW, mapUri);
                    mapIntent.setPackage("com.google.android.apps.maps");
                    context.startActivity(mapIntent);*/
                });
                holder.done.setOnClickListener(v -> {
                    changeDeliveredStatus(pack.id);
                });
            }
        } else {
            if ((pack.status.equals("ReceivedByCustomer") || pack.status.equals("Received") || pack.status.equals("Canceled"))) {
                holder.done.setEnabled(false);
            } else {
                if (pack.status.equals("Preparation")) {
                    holder.timeRow.setVisibility(View.VISIBLE);
                    holder.time.setText(addDayes(pack.created_at, 2)+" / " + addDayes(pack.created_at, 5));
                } else {
                    holder.timeRow.setVisibility(View.GONE);
                }
                holder.done.setOnClickListener(v -> {
                    openDialog(pack.id);
                });
            }
        }
    }

    @Override
    public int getItemCount() {
        return values.size();
    }


    @SuppressLint("MissingPermission")
    public Location getLocation() {
        LocationManager mLocationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        return mLocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
    }

    String addDayes(String date, int days) {
        SimpleDateFormat dateParser = new SimpleDateFormat("yyyy-MM-dd");
        String newFormattedDate = null;
        try {
            Date myDate = dateParser.parse(date);
            Calendar c = Calendar.getInstance();
            c.setTime(myDate);
            c.set(Calendar.DAY_OF_YEAR, c.get(Calendar.DAY_OF_YEAR) + days);
            Date newDate = c.getTime();
            newFormattedDate = dateParser.format(newDate);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return newFormattedDate.substring(5, 10);
    }

    public void getRoute(Point origin, Point destination) {
        NavigationRoute.builder(context)
                .accessToken(Mapbox.getAccessToken())
                .origin(origin)
                .destination(destination)
                .language(Locale.ENGLISH)
                .build()
                .getRoute(new Callback<DirectionsResponse>() {
                    @Override
                    public void onResponse(Call<DirectionsResponse> call, retrofit2.Response<DirectionsResponse> response) {

                        Log.d("Map", "Response code: " + response.code());
                        if (response.body() == null) {
                            Log.e("Map", "No routes found, make sure you set the right user and access token.");
                            return;
                        } else if (response.body().routes().size() < 1) {
                            Log.e("Map", "No routes found");
                            return;
                        }

                        DirectionsRoute currentRoute = response.body().routes().get(0);
                        NavigationLauncherOptions options = NavigationLauncherOptions.builder()
                                .directionsRoute(currentRoute)
                                .shouldSimulateRoute(false)
                                .build();
                        NavigationLauncher.startNavigation((Activity) context, options);

                        if (dialog.isShowing()) {
                            dialog.dismiss();
                        }
                    }


                    @Override
                    public void onFailure(Call<DirectionsResponse> call, Throwable throwable) {
                        Log.e("Map", "Error: " + throwable.getMessage());
                    }
                });
    }

    void openDialog(int id) {

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        LayoutInflater factory = LayoutInflater.from(context);
        final View view = factory.inflate(R.layout.feadback_dialog, null);
        builder.setView(view);
        AlertDialog dialog = builder.create();
        dialog.show();
        Log.i("Test ", "id : " + id);
        RatingBar stars = view.findViewById(R.id.ratingBar);
        EditText comentEdit = view.findViewById(R.id.comentEdit);
        view.findViewById(R.id.submit).setOnClickListener(v -> {
            float star = stars.getRating();
            String comment = comentEdit.getText().toString().trim();
            updateStatusAndFeedback(id, star, comment);
        });
        view.findViewById(R.id.close).setOnClickListener(v -> dialog.dismiss());
    }


    void updateStatusAndFeedback(int id, float star, String comment) {
        Log.i("Test ", "updateStatusAndFeedback : id=" + id + " star=" + star + " comment=" + comment);
        Map<String, String> params = new HashMap();
        params.put("package", String.valueOf(id));
        if (star > 0.0)
            params.put("stars", String.valueOf(star));
        if (!comment.isEmpty())
            params.put("comment", comment);
        JSONObject parameters = new JSONObject(params);
        JsonObjectRequest req = new JsonObjectRequest(Request.Method.PUT, context.getString(R.string.appLinkCustomer) + "updateStatusAndFeedback", parameters,
//        JsonObjectRequest req = new JsonObjectRequest(Request.Method.PUT, AppInfo.AppLinkCustomer + "updateStatusAndFeedback", parameters,
                response -> {
                    try {
                        if (response.getString("status").equals("Success")) {
                            DynamicToast.makeSuccess(context, response.getString("message"), Toast.LENGTH_LONG).show();
                        } else {
                            DynamicToast.makeError(context, response.getString("message"), Toast.LENGTH_LONG).show();
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    ((Activity) context).finish();
                    context.startActivity(((Activity) context).getIntent());
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

    void changeDeliveredStatus(int id) {
        Log.i("Test ", "changeDeliveredStatus : " + id);
        Map<String, Integer> params = new HashMap();
        params.put("package", id);
        JSONObject parameters = new JSONObject(params);
        JsonObjectRequest req = new JsonObjectRequest(Request.Method.PUT, context.getString(R.string.appLink) + "updateDeliveryStatus", parameters,
//        JsonObjectRequest req = new JsonObjectRequest(Request.Method.PUT, AppInfo.AppLinkDelivery + "updateDeliveryStatus", parameters,
                response -> {
                    try {
                        if (response.getString("status").equals("Success")) {
                            DynamicToast.makeSuccess(context, response.getString("message"), Toast.LENGTH_LONG).show();
                        } else {
                            DynamicToast.makeError(context, response.getString("message"), Toast.LENGTH_LONG).show();
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    ((Activity) context).finish();
                    context.startActivity(((Activity) context).getIntent());
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
}
