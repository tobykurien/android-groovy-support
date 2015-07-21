package com.tobykurien.androidgroovysupport.sampleapp

import android.os.Bundle
import android.support.annotation.Nullable
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.tobykurien.androidgroovysupport.db.DbService
import com.tobykurien.androidgroovysupport.sampleapp.model.Webapp
import com.tobykurien.androidgroovysupport.utils.AlertUtils
import com.tobykurien.androidgroovysupport.utils.Async
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
        button.onClickListener = { v ->
            confirm("Are you sure you want to exit?") {
                activity.finish()
            }
        }

        def textview = view.findViewById(R.id.textview) as TextView
        Async.background {
            // This closure runs in a background thread, all other closures run in UI thread
            Thread.sleep(2_000)

            // Database sample:
            def db = DbService.getInstance(activity, "test", 1)
            return db.findAll("webapps", "name", Webapp)
        } first {
            // this runs before the background task in the UI thread
            textview.setText("Loading database entries...")
        } then { List<Webapp> result ->
            // This runs in the UI thread after the background task
            button.enabled = true

            // show db results in textview
            def sb = new StringBuilder()
            result.each { w ->
                sb.append("\r\n ${w.name}: ${w.url}")
            }
            textview.setText(sb.toString())
        } onError { error ->
            // This runs if an error occurs in any closure
            toast("ERROR! ${error.class.name} ${error.message}")
        } execute()

        def settings = Settings.getSettings(activity)
        if (settings.token == null) {
            toast("Creating new token")
            settings.token = UUID.randomUUID()
        } else {
            toast("Found token ${settings.token}")
        }
    }
}