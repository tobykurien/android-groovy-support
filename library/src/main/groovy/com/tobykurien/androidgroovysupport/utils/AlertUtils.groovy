package com.tobykurien.androidgroovysupport.utils

import android.app.AlertDialog
import android.content.Context
import android.support.v4.app.Fragment
import android.widget.Toast
import groovy.transform.CompileStatic

@CompileStatic
trait AlertUtils {
    public void toastShort(String message) {
        toastMessage(message, Toast.LENGTH_SHORT)
    }

    public void toast(String message) {
        toastMessage(message, Toast.LENGTH_LONG)
    }

    private toastMessage(String message, int length) {
        Context context = getContext()
        Toast.makeText(context, message, length).show()
    }

    public void confirm(String message, Closure onConfirm) {
        new AlertDialog.Builder(getContext())
                .setMessage(message)
                .setPositiveButton(android.R.string.ok, {di, i -> onConfirm.call()})
                .setNegativeButton(android.R.string.cancel, null)
                .create()
                .show()
    }

    private Context getContext() {
        if (this == null) {
            throw new IllegalStateException("Receiver object is null")
        } else {
            if (this instanceof Fragment) {
                return (this as Fragment).activity
            } else if (this instanceof android.app.Fragment) {
                return (this as android.app.Fragment).activity
            } else if (this instanceof Context) {
                return this as Context
            } else {
                throw new IllegalStateException("Invalid receiver object " + this.class.name)
            }
        }
    }
}