package com.tobykurien.androidgroovysupport.sampleapp

import android.os.Bundle
import android.os.PersistableBundle
import android.support.v7.app.AppCompatActivity
import com.tobykurien.androidgroovysupport.utils.AlertUtils

class MainActivity extends AppCompatActivity implements AlertUtils {
    @Override
    void onCreate(Bundle savedInstanceState, PersistableBundle persistentState) {
        super.onCreate(savedInstanceState, persistentState)
        setContentView(R.layout.activity_main)
    }

    @Override
    protected void onStart() {
        super.onStart()
        toast("started!")
    }
}