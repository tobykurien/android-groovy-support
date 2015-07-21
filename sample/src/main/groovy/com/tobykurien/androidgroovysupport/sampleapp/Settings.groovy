package com.tobykurien.androidgroovysupport.sampleapp

import android.content.Context
import com.tobykurien.androidgroovysupport.utils.BasePreferences
import groovy.transform.CompileStatic

/**
 * Class to save and read shared preferences
 */
@CompileStatic
class Settings extends BasePreferences {
    private String token = null

    static Settings getSettings(Context context) {
        getPreferences(context, Settings)
    }

    String getToken() {
        return pref.getString("token", token)
    }

    void setToken(String token) {
        pref.edit().putString("token", token).commit()
    }
}