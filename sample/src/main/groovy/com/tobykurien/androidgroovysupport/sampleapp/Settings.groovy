package com.tobykurien.androidgroovysupport.sampleapp

import android.content.Context
import com.tobykurien.androidgroovysupport.utils.BasePreferences

/**
 * Class to save and read shared preferences
 */
class Settings extends BasePreferences {
    private String token = null

    static Settings getSettings(Context context) {
        getPreferences(context, Settings)
    }

    String getToken() {
        return getProperty("token")
    }

    void setToken(String token) {
        setProperty("token", token)
    }
}