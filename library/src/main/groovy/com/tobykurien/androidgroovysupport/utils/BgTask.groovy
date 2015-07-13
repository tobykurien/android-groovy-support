package com.tobykurien.androidgroovysupport.utils

import android.os.AsyncTask
import android.os.Build
import android.util.Log
import groovy.transform.CompileStatic
import groovy.transform.stc.ClosureParams
import groovy.transform.stc.FirstParam
import groovy.transform.stc.SimpleType

@CompileStatic
class BgTask<T> extends AsyncTask<Object, Void, T> {
    Closure<T> inBg
    Closure<Void> onUi
    Closure<Void> onError
    Exception error

    public void runInBg(Closure<T> bg,
                        @ClosureParams(FirstParam.FirstGenericType.class) Closure<Void> onUi,
                        @ClosureParams(value=SimpleType.class, options="java.lang.Exception") Closure<Void> onError) {
        this.inBg = bg
        this.onUi = onUi
        this.onError = onError

        if (bg == null) throw new IllegalArgumentException("background closure cannot be null")

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            // newer versions of Android use a single thread, rather default to multiple threads
            executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR)
        } else {
            // older versions of Android already use a thread pool
            execute()
        }
    }

    @Override
    protected T doInBackground(Object... objects) {
        try {
            return inBg()
        } catch (Exception e) {
            error = e
        }
        return null as T
    }

    @Override
    protected void onPostExecute(T result) {
        if (error != null) {
            if (!isCancelled() && onError != null) try {
                onError(error)
            } catch (Exception e) {
                // ignore
                Log.e("BgTask", "Error executing error closure", e)
            }
        } else if (!isCancelled()) {
            try {
                if(onUi != null) onUi(result)
            } catch (Exception e) {
                if (onError != null) try {
                    onError(e)
                } catch (Exception e2) {
                    // ignore
                    Log.e("BgTask", "Error executing error closure", e2)
                }
            }
        }
    }
}