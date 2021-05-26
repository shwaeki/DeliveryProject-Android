package com.shwaeki.delivery.Adapters;


import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.location.Location;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.mapbox.geojson.Feature;
import com.mapbox.geojson.Point;
import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource;
import com.shwaeki.delivery.Activities.MapActivity;
import com.shwaeki.delivery.Activities.PackageActivity;
import com.shwaeki.delivery.DeliveryInfo;
import com.shwaeki.delivery.Models.Package;
import com.shwaeki.delivery.R;
import com.shwaeki.delivery.SharedPrefer;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;


public class MapPackageAdapter extends RecyclerView.Adapter<MapPackageAdapter.ViewHolder> {
    private ArrayList<Package> values;
    private Context context;
    private MapboxMap map;
    private Location myLocation;
    private int selected_position = -1;

    public MapPackageAdapter(Context context, ArrayList<Package> values, MapboxMap map, Location myLocation) {
        this.values = values;
        this.context = context;
        this.map = map;
        this.myLocation = myLocation;
    }


    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public TextView name, customer, phone, distance;
        public View layout;
        public CardView packageCard;


        public ViewHolder(View v) {
            super(v);
            layout = v;
            layout.setOnClickListener(this);
            name = v.findViewById(R.id.name);
            customer = v.findViewById(R.id.customer);
            phone = v.findViewById(R.id.phone);
            distance = v.findViewById(R.id.distance);
            packageCard = v.findViewById(R.id.package_card);
        }

        @Override
        public void onClick(View v) {
            int position = getLayoutPosition();
            double Lat = Double.parseDouble(values.get(position).customer.latitude);
            double Lng = Double.parseDouble(values.get(position).customer.longitude);
            map.setCameraPosition(new CameraPosition.Builder().target(new LatLng(Lat, Lng)).zoom(11.0).bearing(0).build());

            if (selected_position == position) {
                selected_position = -1;
                notifyDataSetChanged();
                return;
            }
            selected_position = position;
            notifyDataSetChanged();

            Point originPoint = Point.fromLngLat(DeliveryInfo.lng, DeliveryInfo.lat);
            Point destinationPoint = Point.fromLngLat(Lng, Lat);
            ((MapActivity) context).getRoute(originPoint, destinationPoint);
        }
    }


    @Override
    public MapPackageAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int i) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View v = inflater.inflate(R.layout.map_package_row, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {
        Package pack = values.get(position);
        if (selected_position == position) {
            holder.packageCard.setCardBackgroundColor(Color.parseColor("#eeeeee"));
        } else {
            holder.packageCard.setCardBackgroundColor(Color.WHITE);
        }

        holder.name.setText(pack.name);
        holder.customer.setText(pack.customer.name);
        holder.phone.setText(pack.customer.phone);
/*
        Location MyLocation = new Location("");
        MyLocation.setLatitude(DeliveryInfo.lat);
        MyLocation.setLongitude(DeliveryInfo.lng);
*/

        Location loc1 = new Location("");
        loc1.setLatitude(Double.parseDouble(pack.customer.latitude));
        loc1.setLongitude(Double.parseDouble(pack.customer.longitude));

        double distance = myLocation.distanceTo(loc1) / 1000;
        holder.distance.setText(String.format("%s KM", new DecimalFormat("##.##").format(distance)));
    }


    @Override
    public int getItemCount() {
        return values.size();
    }

}
