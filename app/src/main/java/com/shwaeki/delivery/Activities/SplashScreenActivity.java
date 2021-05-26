package com.shwaeki.delivery.Activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.shwaeki.delivery.AppInfo;
import com.shwaeki.delivery.R;
import com.shwaeki.delivery.SharedPrefer;

public class SplashScreenActivity extends AppCompatActivity {
    private final int SPLASH_DISPLAY_LENGTH = 2000;
//    private FirebaseRemoteConfig mFirebaseRemoteConfig;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                SharedPrefer sharedPrefer = new SharedPrefer(SplashScreenActivity.this);
                if (sharedPrefer.LoadStringData("access_token") != null) {
                    Intent intent = new Intent(SplashScreenActivity.this, DeliveryMainActivity.class);
                    sharedPrefer.SaveStringData("access_token_customer", null);
                    startActivity(intent);
                    finish();
                } else if (sharedPrefer.LoadStringData("access_token_customer") != null) {
                    Intent intent = new Intent(SplashScreenActivity.this, CustomerMainActivity.class);
                    sharedPrefer.SaveStringData("access_token", null);
                    startActivity(intent);
                    finish();
                } else {
                    Intent intent = new Intent(SplashScreenActivity.this, MainActivity.class);
                    startActivity(intent);
                    finish();
                }
            }
        }, SPLASH_DISPLAY_LENGTH);

/*
        mFirebaseRemoteConfig = FirebaseRemoteConfig.getInstance();
        mFirebaseRemoteConfig.fetch(0);
        mFirebaseRemoteConfig.fetchAndActivate().addOnCompleteListener(this, task -> {
            if (task.isSuccessful()) {
                String link1 = mFirebaseRemoteConfig.getString("AppLinkCustomer");
                String link2 = mFirebaseRemoteConfig.getString("AppLinkDelivery");
                AppInfo.AppLinkCustomer = link1;
                AppInfo.AppLinkDelivery = link2;
                Log.i("Test", "App Link Customer:" + link1);
                Log.i("Test", "App Link Delivery:" + link2);

            }
        });*/


    }
}