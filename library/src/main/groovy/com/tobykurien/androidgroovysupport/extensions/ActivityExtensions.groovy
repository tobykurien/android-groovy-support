package com.tobykurien.androidgroovysupport.extensions

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.support.v4.app.Fragment
import android.widget.Toast
import groovy.transform.CompileStatic

@CompileStatic
class ActivityExtensions {
    static void toastShort(Context self, String message) {
        toastMessage(self, message, Toast.LENGTH_SHORT)
    }

    static void toast(Context self, String message) {
        toastMessage(self, message, Toast.LENGTH_LONG)
    }

    static toastMessage(Context self, String message, int length) {
        Toast.makeText(self, message, length).show()
    }

    static void toastShort(Fragment self, String message) {
        toastMessage(self.activity, message, Toast.LENGTH_SHORT)
    }

    static void toast(Fragment self, String message) {
        toastMessage(self.activity, message, Toast.LENGTH_LONG)
    }

    static void toastShort(android.app.Fragment self, String message) {
        toastMessage(self.activity, message, Toast.LENGTH_SHORT)
    }

    static void toast(android.app.Fragment self, String message) {
        toastMessage(self.activity, message, Toast.LENGTH_LONG)
    }

    static void confirm(Fragment self, String message, Closure onConfirm) {
        confirm(self.activity, message, onConfirm)
    }

    static void confirm(android.app.Fragment self, String message, Closure onConfirm) {
        confirm(self.activity, message, onConfirm)
    }

    static void confirm(Activity self, String message, Closure onConfirm) {
        new AlertDialog.Builder(self)
                .setMessage(message)
                .setPositiveButton(android.R.string.ok, {di, i -> onConfirm.call()})
                .setNegativeButton(android.R.string.cancel, null)
                .create()
                .show()
    }
}