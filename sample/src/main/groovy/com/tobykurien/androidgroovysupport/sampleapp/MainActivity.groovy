package com.tobykurien.androidgroovysupport.sampleapp

import android.os.Bundle
import android.support.v7.app.AppCompatActivity

import groovy.transform.CompileStatic

@CompileStatic
class MainActivity extends AppCompatActivity {
    @Override
    void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }
}