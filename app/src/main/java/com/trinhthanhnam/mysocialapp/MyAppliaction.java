package com.trinhthanhnam.mysocialapp;

import com.google.firebase.database.FirebaseDatabase;

public class MyAppliaction extends android.app.Application{
    @Override
    public void onCreate() {
        super.onCreate();
        FirebaseDatabase.getInstance().setPersistenceEnabled(true);
    }
}
