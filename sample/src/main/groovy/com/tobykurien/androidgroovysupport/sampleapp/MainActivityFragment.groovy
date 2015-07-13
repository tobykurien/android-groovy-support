package com.tobykurien.androidgroovysupport.sampleapp

import android.os.Bundle
import android.support.annotation.Nullable
import android.support.v4.app.Fragment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.tobykurien.androidgroovysupport.db.DbService
import com.tobykurien.androidgroovysupport.sampleapp.model.Webapp
import com.tobykurien.androidgroovysupport.utils.AlertUtils
import com.tobykurien.androidgroovysupport.utils.BgTask
import groovy.transform.CompileStatic

@CompileStatic
class MainActivityFragment extends Fragment implements AlertUtils {
    @Override
    View onCreateView(LayoutInflater inflater,
                      @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_main, container, false)
    }

    @Override
    void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState)

        def button = view.findViewById(R.id.btnExit)
        button.enabled = false
        button.onClickListener = {v ->
            confirm("Are you sure you want to exit?") {
                activity.finish()
            }
        }

        def textview = view.findViewById(R.id.textview) as TextView
        // run a background task that returns a string
        new BgTask<List<Webapp>>().runInBg({
            // This runs in a background thread
            Thread.sleep(2_000)

            // Database sample:
            def db = DbService.getInstance(activity, "test", 1)
            return db.findAll("webapps", "name", Webapp)
        }, { result ->
            // This runs in the UI thread
            button.enabled = true

            // show db results in textview
            def sb = new StringBuilder()
            result.each { w ->
                sb.append("\r\n ${w.name}: ${w.url}")
            }
            textview.setText(sb.toString())
        }, { error ->
            // This runs in the UI thread if an error occurs during background processing
            toast(error.message)
        })
    }
}