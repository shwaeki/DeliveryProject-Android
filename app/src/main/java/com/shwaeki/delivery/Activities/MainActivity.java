package com.shwaeki.delivery.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings;
import com.shwaeki.delivery.AppInfo;
import com.shwaeki.delivery.R;
import com.shwaeki.delivery.SharedPrefer;

public class MainActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

    }

    public void GoToDelivery(View view) {
        Intent a = new Intent(getApplicationContext(), LoginActivity.class);
        startActivity(a);
    }

    public void GoToCustomer(View view) {
        Intent a = new Intent(getApplicationContext(), CustomerLoginActivity.class);
        startActivity(a);
    }

}