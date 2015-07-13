package com.tobykurien.androidgroovysupport.sampleapp

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.tobykurien.androidgroovysupport.utils.AlertUtils
import groovy.transform.CompileStatic

@CompileStatic
class MainActivity extends AppCompatActivity implements AlertUtils {
    @Override
    void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }
}