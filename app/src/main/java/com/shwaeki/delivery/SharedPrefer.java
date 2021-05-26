package com.shwaeki.delivery;

import android.content.Context;
import android.content.SharedPreferences;

public class SharedPrefer {
    SharedPreferences ShredRef;

    public SharedPrefer(Context context) {
        ShredRef = context.getSharedPreferences("Token", Context.MODE_PRIVATE);
    }


    public void SaveStringData(String Data, String Value) {

        SharedPreferences.Editor editor = ShredRef.edit();
        editor.putString(Data, Value);
        editor.apply();
    }

    public String LoadStringData(String Data) {
        return ShredRef.getString(Data, null);
    }

    public void SaveIntData(String Data, int Value) {
        SharedPreferences.Editor editor = ShredRef.edit();
        editor.putInt(Data, Value);
        editor.apply();
    }

    public int LoadIntData(String Data) {
        return ShredRef.getInt(Data, 0);
    }

}