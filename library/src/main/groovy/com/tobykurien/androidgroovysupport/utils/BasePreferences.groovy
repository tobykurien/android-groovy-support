package com.tobykurien.androidgroovysupport.utils

import android.content.Context
import android.content.SharedPreferences
import android.preference.PreferenceManager
import groovy.transform.CompileStatic

import java.util.WeakHashMap

/**
 * A base class for easy access to SharedPreferences. Implements caching of
 * SharedPreferences instances. Use in conjunction with the @Preference
 * annotation
 */
@CompileStatic
class BasePreferences {
    protected SharedPreferences pref
    protected static WeakHashMap cache = new WeakHashMap<Integer, BasePreferences>()

    protected BasePreferences() {
    }

    static <T extends BasePreferences> T getPreferences(Context context, Class<T> subclass) {
        if(cache.keySet().size() > 5) cache.clear() // avoid memory leaks by clearing often
        if (cache.get(context.hashCode()) == null) {
            def preferences = PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext())
            cache.put(context.hashCode(), newInstance(subclass, preferences))
        }

        cache.get(context.hashCode()) as T
    }

    private setPref(SharedPreferences preferences) {
        pref = preferences
    }

    static newInstance(Class<?> cls, SharedPreferences preferences) {
        def BasePreferences instance

        try {
            instance = cls.newInstance() as BasePreferences;
        } catch (ClassCastException cce) {
            throw new IllegalArgumentException(
                    "BasePreferences: Class ${cls.getName()} is not a subclass of BasePreferences?");
        } catch (Exception ex) {
            throw new IllegalStateException(
                    "BasePreferences: Could not instantiate object (no default constructor?) for ${cls.getName()}: ${ex.getMessage()}", ex);
        }

        instance.setPref(preferences)
        return instance;
    }

    static clearCache() {
        cache.clear()
    }
}
